package com.workbuddy.matrixai.intelligence_service.controller;

import com.workbuddy.matrixai.intelligence_service.config.LocalModelProperties;
import com.workbuddy.matrixai.intelligence_service.dto.chat.ChatRequest;
import com.workbuddy.matrixai.intelligence_service.dto.chat.ChatResponse;
import com.workbuddy.matrixai.intelligence_service.dto.chat.StreamResponse;
import com.workbuddy.matrixai.intelligence_service.dto.model.OpenRouterModelInfo;
import com.workbuddy.matrixai.intelligence_service.service.AiGenerationService;
import com.workbuddy.matrixai.intelligence_service.service.ChatService;
import com.workbuddy.matrixai.intelligence_service.service.OpenRouterModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final AiGenerationService aiGenerationService;
    private final ChatService chatService;
    private final OpenRouterModelService openRouterModelService;
    private final LocalModelProperties localModelProperties;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamResponse>> streamChat(
            @RequestBody ChatRequest request) {

        return aiGenerationService.streamResponse(request.message(), request.projectId(), request.model())
                .map(data -> ServerSentEvent.<StreamResponse>builder()
                        .data(data)
                        .build());
    }

    @GetMapping("/models")
    public ResponseEntity<List<OpenRouterModelInfo>> listModels() {
        List<OpenRouterModelInfo> models = new ArrayList<>(openRouterModelService.listModels());

        if (localModelProperties.isEnabled()) {
            models.add(new OpenRouterModelInfo(
                    localModelProperties.getModelId(),
                    localModelProperties.getDisplayName(),
                    localModelProperties.getContextLength(),
                    true,
                    0.0,
                    0.0
            ));
        }

        return ResponseEntity.ok(models);
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<ChatResponse>> getChatHistory(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(chatService.getProjectChatHistory(projectId));
    }
}
