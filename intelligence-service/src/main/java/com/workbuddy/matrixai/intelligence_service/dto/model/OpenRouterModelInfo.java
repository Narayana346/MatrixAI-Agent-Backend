package com.workbuddy.matrixai.intelligence_service.dto.model;

public record OpenRouterModelInfo(
        String id,
        String name,
        Integer contextLength,
        boolean free,
        double promptPricePerMillionTokens,
        double completionPricePerMillionTokens
) {}
