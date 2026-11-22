package com.workbuddy.matrixai.intelligence_service.service;

import com.workbuddy.matrixai.intelligence_service.dto.chat.StreamResponse;
import reactor.core.publisher.Flux;

public interface AiGenerationService {
    // selectedModel is an OpenRouter model id (e.g. "openai/gpt-5.1"); pass null/blank
    // to use the configured default.
    Flux<StreamResponse> streamResponse(String message, Long projectId, String selectedModel);
}
