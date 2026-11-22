package com.workbuddy.matrixai.intelligence_service.service.impl;

import com.workbuddy.matrixai.common_lib.enums.ChatEventStatus;
import com.workbuddy.matrixai.common_lib.enums.ChatEventType;
import com.workbuddy.matrixai.common_lib.enums.MessageRole;
import com.workbuddy.matrixai.common_lib.error.ResourceNotFoundException;
import com.workbuddy.matrixai.common_lib.event.FileStoreRequestEvent;
import com.workbuddy.matrixai.common_lib.security.AuthUtil;
import com.workbuddy.matrixai.intelligence_service.client.WorkspaceClient;
import com.workbuddy.matrixai.intelligence_service.dto.chat.StreamResponse;
import com.workbuddy.matrixai.intelligence_service.entity.ChatEvent;
import com.workbuddy.matrixai.intelligence_service.entity.ChatMessage;
import com.workbuddy.matrixai.intelligence_service.entity.ChatSession;
import com.workbuddy.matrixai.intelligence_service.entity.ChatSessionId;
import com.workbuddy.matrixai.intelligence_service.llm.CodeGenerationTools;
import com.workbuddy.matrixai.intelligence_service.llm.FileTreeContextAdvisor;
import com.workbuddy.matrixai.intelligence_service.llm.LlmResponseParser;
import com.workbuddy.matrixai.intelligence_service.llm.PromptUtils;
import com.workbuddy.matrixai.intelligence_service.repository.ChatEventRepository;
import com.workbuddy.matrixai.intelligence_service.repository.ChatMessageRepository;
import com.workbuddy.matrixai.intelligence_service.repository.ChatSessionRepository;
import com.workbuddy.matrixai.intelligence_service.service.AiGenerationService;
import com.workbuddy.matrixai.intelligence_service.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationServiceImpl implements AiGenerationService {

    private final ChatClient chatClient;
    @Qualifier("fallbackChatClient")
    private final ChatClient fallbackChatClient;
    private final AuthUtil authUtil;
    private final FileTreeContextAdvisor fileTreeContextAdvisor;
    private final ChatSessionRepository chatSessionRepository;
    private final LlmResponseParser llmResponseParser;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatEventRepository chatEventRepository;
    private final UsageService usageService;
    private final WorkspaceClient workspaceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<StreamResponse> streamResponse(String userMessage, Long projectId, String selectedModel) {

//        usageService.checkDailyTokensUsage();

        Long userId = authUtil.getCurrentUserId();
        // Captured here, on the authenticated request thread, and threaded explicitly into
        // the advisor/tools below — they may run on a different thread once the reactive
        // pipeline is subscribed, where SecurityContextHolder's ThreadLocal would be empty.
        String jwtToken = authUtil.getCurrentToken();
        ChatSession chatSession = createChatSessionIfNotExists(projectId, userId);

        Map<String, Object> advisorParams = Map.of(
                "userId", userId,
                "projectId", projectId,
                "jwtToken", jwtToken
        );

        CodeGenerationTools codeGenerationTools = new CodeGenerationTools(projectId, workspaceClient, authUtil, jwtToken);

        StringBuilder fullResponseBuffer = new StringBuilder();
        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long> endTime = new AtomicReference<>(0L);
        AtomicReference<Usage> usageRef = new AtomicReference<>();

        return streamFrom(chatClient, selectedModel, userMessage, projectId, advisorParams, codeGenerationTools,
                chatSession, userId, fullResponseBuffer, startTime, endTime, usageRef)
                .onErrorResume(error -> {
                    // Only retry on the fallback model if the primary one failed before
                    // producing any output (e.g. an upfront 429) — restarting after a
                    // partial stream would duplicate/garble what the user already saw.
                    if (fullResponseBuffer.length() > 0) {
                        return Flux.error(error);
                    }
                    log.warn("Primary chat model failed for projectId {} ({}), retrying with fallback model", projectId, error.getMessage());
                    startTime.set(System.currentTimeMillis());
                    endTime.set(0L);
                    // The fallback client always uses its own dedicated low-latency model,
                    // regardless of what the user selected — that's the whole point of a fallback.
                    return streamFrom(fallbackChatClient, null, userMessage, projectId, advisorParams, codeGenerationTools,
                            chatSession, userId, fullResponseBuffer, startTime, endTime, usageRef);
                });
    }

    private Flux<StreamResponse> streamFrom(ChatClient client, String modelOverride, String userMessage, Long projectId,
                                             Map<String, Object> advisorParams, CodeGenerationTools codeGenerationTools,
                                             ChatSession chatSession, Long userId, StringBuilder fullResponseBuffer,
                                             AtomicReference<Long> startTime, AtomicReference<Long> endTime,
                                             AtomicReference<Usage> usageRef) {
        ChatClient.ChatClientRequestSpec requestSpec = client.prompt()
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(codeGenerationTools)
                .advisors(advisorSpec -> {
                            advisorSpec.params(advisorParams);
                            advisorSpec.advisors(fileTreeContextAdvisor);
                        }
                );

        if (StringUtils.hasText(modelOverride)) {
            requestSpec = requestSpec.options(OpenAiChatOptions.builder().model(modelOverride).build());
        }

        return requestSpec
                .stream()
                .chatResponse()
                .doOnNext(response -> {
                    if (response.getResults() != null && !response.getResults().isEmpty()) {
                        String content = response.getResult().getOutput().getText();

                        if(content != null && !content.isEmpty() && endTime.get() == 0) { // first non-empty chunk received
                            endTime.set(System.currentTimeMillis());
                        }
                        if(response.getMetadata().getUsage() != null) {
                            usageRef.set(response.getMetadata().getUsage());
                        }
                        // StringBuilder#append(null) writes the literal text "null" rather than
                        // doing nothing, which would otherwise make an empty/role-only preamble
                        // chunk look like real content (e.g. to the fallback's "did we already
                        // produce output" check below).
                        if (content != null) {
                            fullResponseBuffer.append(content);
                        }
                    }

                })
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(() -> {
//                        parseAndSaveFiles(fullResponseBuffer.toString(), projectId);

                        long duration = (endTime.get() - startTime.get()) /  1000;
                        finalizeChats(userMessage, chatSession, fullResponseBuffer.toString(), duration, usageRef.get(), userId);
                    });
                })
                .doOnError(error -> log.error("Error during streaming for projectId: {}", projectId))
                .map(response -> {
                    if (response.getResults() != null && !response.getResults().isEmpty()) {
                        String text = response.getResult().getOutput().getText();
                        return new StreamResponse(text != null ? text : "");
                    }
                    return new StreamResponse("");
                });
    }

    private void finalizeChats(String userMessage, ChatSession chatSession, String fullText, Long duration, Usage usage, Long userId) {
        Long projectId = chatSession.getId().getProjectId();

        if(usage != null) {
            int totalTokens = usage.getTotalTokens();
            usageService.recordTokenUsage(chatSession.getId().getUserId(), totalTokens);
        }

        // Save the User message
        chatMessageRepository.save(
                ChatMessage.builder()
                        .chatSession(chatSession)
                        .role(MessageRole.USER)
                        .content(userMessage)
                        .tokensUsed(usage.getPromptTokens())
                        .build()
        );

        ChatMessage assistantChatMessage = ChatMessage.builder()
                .role(MessageRole.ASSISTANT)
                .content("Assistant Message here...")
                .chatSession(chatSession)
                .tokensUsed(usage.getCompletionTokens())
                .build();

        assistantChatMessage = chatMessageRepository.save(assistantChatMessage);

        List<ChatEvent> chatEventList = llmResponseParser.parseChatEvents(fullText, assistantChatMessage);
        chatEventList.addFirst(ChatEvent.builder()
                        .type(ChatEventType.THOUGHT)
                        .status(ChatEventStatus.CONFIRMED)
                        .chatMessage(assistantChatMessage)
                        .content("Thought for "+duration+"s")
                        .sequenceOrder(0)
                .build());

        chatEventList.stream()
                .filter(e -> e.getType() == ChatEventType.FILE_EDIT)
                .forEach(e -> {
                    String sagaId = UUID.randomUUID().toString();
                    e.setSagaId(sagaId);
                    FileStoreRequestEvent fileStoreRequestEvent = new FileStoreRequestEvent(
                            projectId,
                            sagaId,
                            e.getFilePath(),
                            e.getContent(),
                            userId
                    );
                    log.info("Storage request event sent: {}", e.getFilePath());
                    kafkaTemplate.send("file-storage-request-event", "project-"+projectId, fileStoreRequestEvent);
                });

        chatEventRepository.saveAll(chatEventList);
    }

    private ChatSession createChatSessionIfNotExists(Long projectId, Long userId) {
        ChatSessionId chatSessionId = new ChatSessionId(projectId, userId);
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId).orElse(null);

        if(chatSession == null) {
            chatSession = ChatSession.builder()
                    .id(chatSessionId)
                    .build();

            chatSession = chatSessionRepository.save(chatSession);
        }
        return chatSession;
    }
}