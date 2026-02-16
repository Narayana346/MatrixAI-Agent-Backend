package com.workbuddy.matrix.controller;

import com.workbuddy.matrix.dto.chat.ChatRequest;
import com.workbuddy.matrix.service.AiGenerationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ChatController {
    AiGenerationService aiGenerationService;

    @PostMapping(value = "/api/chat/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestBody @Valid ChatRequest request){
        return aiGenerationService.stremResponse(request.message(),request.projectId())
                .map(data -> ServerSentEvent
                        .builder()
                        .data(data)
                        .build()
                );
    }
}
