package com.workbuddy.matrixai.intelligence_service.service;

import com.workbuddy.matrixai.intelligence_service.dto.chat.StreamResponse;
import reactor.core.publisher.Flux;

public interface AiGenerationService {
    Flux<StreamResponse> streamResponse(String message, Long projectId);
}
