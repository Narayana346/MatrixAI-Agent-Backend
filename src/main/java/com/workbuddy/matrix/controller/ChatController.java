package com.workbuddy.matrix.controller;

import com.workbuddy.matrix.dto.chat.ChatRequest;
import com.workbuddy.matrix.dto.chat.ChatResponse;
import com.workbuddy.matrix.dto.chat.StreamResponse;
import com.workbuddy.matrix.service.AiGenerationService;
import com.workbuddy.matrix.service.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequestMapping("/api/chat")
public class ChatController {
    AiGenerationService aiGenerationService;
    ChatService chatService;

    @PostMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamResponse>> streamChat(@RequestBody @Valid ChatRequest request){
        return aiGenerationService.stremResponse(request.message(),request.projectId())
                .map(data -> ServerSentEvent
                        .<StreamResponse>builder()
                        .data(data)
                        .build()
                );
    }

    @GetMapping("projects/{projectId}")
    public ResponseEntity<List<ChatResponse>> getProjectChatHistory(@PathVariable Long projectId){
        return ResponseEntity.ok(chatService.getProjectChatHistory(projectId));
    }
}
