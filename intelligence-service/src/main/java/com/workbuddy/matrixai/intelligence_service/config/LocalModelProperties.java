package com.workbuddy.matrixai.intelligence_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

// Settings for the self-hosted Qwen Coder model, served through Docker Model
// Runner's OpenAI-compatible API (see k8s/stateful/qwen-coder-model-runner.yaml).
// Shared by AiConfig (builds the client), AiGenerationServiceImpl (routes to it)
// and ChatController (lists it alongside the OpenRouter catalog).
//
// Pulled from bartowski's GGUF re-upload rather than Qwen's own repo: Qwen's
// repo exposes the Q4_K_M quant as three files for the same content (one
// whole file plus a redundant pre-split 2-part copy), which DMR's HF tag
// resolver matches ambiguously and fails to load as a bogus 3-part split.
@Data
@ConfigurationProperties(prefix = "ai.chat.local")
public class LocalModelProperties {
    private boolean enabled = false;
    private String modelId = "hf.co/bartowski/Qwen2.5-Coder-7B-Instruct-GGUF:Q4_K_M";
    private String displayName = "Qwen2.5 Coder 7B (Local)";
    private String baseUrl = "http://localhost:12434";
    private String completionsPath = "/engines/v1/chat/completions";
    private String apiKey = "docker-model-runner";
    private Integer contextLength = 32768;
}
