package com.workbuddy.matrixai.intelligence_service.dto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Raw shape of OpenRouter's GET /v1/models response — internal, mapped to
// OpenRouterModelInfo before being returned to callers.
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenRouterModelsResponse(List<RawModel> data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawModel(
            String id,
            String name,
            @JsonProperty("context_length") Integer contextLength,
            Pricing pricing
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pricing(String prompt, String completion) {}
}
