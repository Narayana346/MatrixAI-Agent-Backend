package com.workbuddy.matrixai.intelligence_service.dto.chat;

public record ChatRequest(String message, Long projectId, String model) {}
