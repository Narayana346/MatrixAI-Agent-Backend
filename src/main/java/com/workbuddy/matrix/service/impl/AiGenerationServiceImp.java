package com.workbuddy.matrix.service.impl;

import aj.org.objectweb.asm.commons.Remapper;
import com.workbuddy.matrix.security.AuthUtil;
import com.workbuddy.matrix.service.AiGenerationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class AiGenerationServiceImp implements AiGenerationService {
    ChatClient chatClient;
    AuthUtil authUtil;

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<String> stremResponse(String userMessage, Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        createChatSessionIfNotExists(userId);

        Map<String,Object> advisorsParams = Map.of(
                "userId",userId,
                "projectId",projectId
        );
        StringBuilder stringBuilder = new StringBuilder();

        return chatClient.prompt()
                .system("SYSTEM_PROMPT_HERE")
                .user(userMessage)
                .advisors( advisorSpec -> {
                    advisorSpec.params(advisorsParams);
                        }

                )
                .stream()
                .chatResponse()
                .doOnNext( chatResponse -> {
                    stringBuilder.append(
                            chatResponse.getResult().getOutput().getText()
                    );
                })
                .doOnComplete(() ->{

                })
                .doOnError(error -> {
                    log.error("Error during streaming for project",error);
                })
                .map(chatResponse -> Objects.requireNonNull(
                        chatResponse.getResult().getOutput().getText())
                );
    }

    private void createChatSessionIfNotExists(Long userId) {
    }
}
