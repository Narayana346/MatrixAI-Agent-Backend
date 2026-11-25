package com.workbuddy.matrixai.intelligence_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

// Settings for the self-hosted local model, served through Docker Model
// Runner's OpenAI-compatible API (see k8s/stateful/qwen-coder-model-runner.yaml).
// Shared by AiConfig (builds the client), AiGenerationServiceImpl (routes to it)
// and ChatController (lists it alongside the OpenRouter catalog).
//
// Llama-3.1-8B-Instruct, not a Qwen Coder model: Qwen2.5-Coder never returns a
// structured tool_calls response from this llama.cpp build (confirmed even with
// tool_choice=required), and Hermes-2-Pro's function-calling format isn't driven
// by the OpenAI "tools" field at all (it needs a hand-built system prompt and
// text-based <tool_call> parsing). Llama-3.1's own chat template has real
// {% if tools %} support and reliably returns finish_reason=tool_calls.
// Its GGUF declares a 131072-token context, which DMR sizes the KV cache to
// (~16GiB) regardless of contextLength below, causing an OOM — the cached blob
// on the PVC has had its embedded llama.context_length metadata binary-patched
// to 32768 to fix this. If this model is ever re-pulled fresh (e.g. PVC/blob
// deleted), that patch is lost and must be reapplied by hand.
@Data
@ConfigurationProperties(prefix = "ai.chat.local")
public class LocalModelProperties {
    private boolean enabled = false;
    private String modelId = "hf.co/unsloth/Llama-3.1-8B-Instruct-GGUF:Q4_K_M";
    private String displayName = "Llama 3.1 8B (Local)";
    private String baseUrl = "http://localhost:12434";
    private String completionsPath = "/engines/v1/chat/completions";
    private String apiKey = "docker-model-runner";
    private Integer contextLength = 32768;
}
