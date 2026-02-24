package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.chat.StreamResponse;
import com.workbuddy.matrix.entity.*;
import com.workbuddy.matrix.enums.ChatEventType;
import com.workbuddy.matrix.enums.MessageRole;
import com.workbuddy.matrix.error.ResourceNotFoundException;
import com.workbuddy.matrix.llm.LLMResponseParser;
import com.workbuddy.matrix.llm.Prompt;
import com.workbuddy.matrix.llm.advisors.FileTreeContextAdvisor;
import com.workbuddy.matrix.llm.tool.CodeGenerationTool;
import com.workbuddy.matrix.repository.*;
import com.workbuddy.matrix.security.AuthUtil;
import com.workbuddy.matrix.service.AiGenerationService;
import com.workbuddy.matrix.service.ProjectFileService;
import com.workbuddy.matrix.service.UsageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class AiGenerationServiceImp implements AiGenerationService {
    ChatClient chatClient;
    AuthUtil authUtil;
    ProjectFileService projectFileService;
    FileTreeContextAdvisor fileTreeContextAdvisor;
    ChatMessageRepository chatMessageRepository;
    ChatEventRepository chatEventRepository;
    ChatSessionRepository chatSessionRepository;
    UserRepository userRepository;
    ProjectRepository projectRepository;
    UsageService usageService;
    LLMResponseParser llmResponseParser;

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<StreamResponse> stremResponse(String userMessage, Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        ChatSession chatSession = createChatSessionIfNotExists(userId,projectId);

        Map<String,Object> advisorsParams = Map.of(
                "userId",userId,
                "projectId",projectId
        );
        StringBuilder fullResponseBuffer = new StringBuilder();

        CodeGenerationTool codeGenerationTool = new CodeGenerationTool(projectFileService,projectId);

        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long> endTime = new AtomicReference<>(0L);
        AtomicReference<Usage> usageRef = new AtomicReference<>();

        return chatClient.prompt()
                .system(Prompt.CODE_GENERATION_SYSTEM_PROMPT)
                .user(userMessage)
                .tools(codeGenerationTool)
                .advisors( advisorSpec -> {
                    advisorSpec.params(advisorsParams);
                    advisorSpec.advisors(fileTreeContextAdvisor);
                })
                .stream()
                .chatResponse()
                .doOnNext( chatResponse -> {
                    log.info("Chat response: {}",chatResponse);
                    String content = chatResponse.getResult().getOutput().getText();
                    if(content != null && !content.isEmpty() && endTime.get() == 0) { // first non-empty chunk received
                        endTime.set(System.currentTimeMillis());
                    }

                    if(chatResponse.getMetadata().getUsage() != null) {
                        usageRef.set(chatResponse.getMetadata().getUsage());
                    }

                    fullResponseBuffer.append(content);
                    fullResponseBuffer.append(content);
                })
                .doOnComplete(() ->{
                    // in here implement background thread as flux way ... but u can use Executor framework
                    Schedulers.boundedElastic().schedule(() -> {

                        long duration = (endTime.get() - startTime.get()) /  1000;
                        finalizeChats(userMessage, chatSession, fullResponseBuffer.toString(),duration,usageRef.get());
                    });
                })
                .doOnError(error -> log.error("Error during streaming for project",error))
                .map(chatResponse -> {
                    String text = chatResponse.getResult().getOutput().getText();
                    return new StreamResponse(text);
                });
    }

    private void finalizeChats(String userMessage, ChatSession chatSession, String fullText, Long duration, Usage usage) {
        Long projectId = chatSession.getProject().getId();

        if(usage != null) {
            int totalTokens = usage.getTotalTokens();
            usageService.recordTokenUsage(chatSession.getUser().getId(), totalTokens);
        }

        // Save the User message
        chatMessageRepository.save(
                ChatMessage.builder()
                        .chatSession(chatSession)
                        .role(MessageRole.USER)
                        .content(userMessage)
                        .chatSession(chatSession)
                        .tokenUsed(Objects.requireNonNull(usage).getPromptTokens())
                        .build()
        );

        ChatMessage assistantChatMessage = ChatMessage.builder()
                .role(MessageRole.ASSISTANT)
                .content("Assistant Message here...")
                .chatSession(chatSession)
                .tokenUsed(usage.getCompletionTokens())
                .build();

        assistantChatMessage = chatMessageRepository.save(assistantChatMessage);

        List<ChatEvent> chatEventList = llmResponseParser.parseChatEvents(fullText, assistantChatMessage);
        chatEventList.addFirst(ChatEvent.builder()
                .eventType(ChatEventType.THOUGHT)
                .chatMessage(assistantChatMessage)
                .content("Thought for "+duration+"s")
                .sequenceOrder(0)
                .build());

        chatEventList.stream()
                .filter(event -> event.getEventType() == ChatEventType.FILE_EDIT)
                .forEach(event -> projectFileService.saveFile(projectId, event.getFilePath(), event.getContent()));

        chatEventRepository.saveAll(chatEventList);
    }

    private ChatSession createChatSessionIfNotExists(Long userId, Long projectId) {
        ChatSessionId chatSessionId = new ChatSessionId(projectId,userId);
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId).orElse(null);
        if(chatSession == null){
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project",projectId.toString()));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User",userId.toString()));

            chatSession = ChatSession.builder()
                    .id(chatSessionId)
                    .project(project)
                    .user(user)
                    .build();

           chatSession = chatSessionRepository.save(chatSession);

        }

        return chatSession;
    }
}
