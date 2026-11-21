package com.workbuddy.matrixai.intelligence_service.dto.chat;


import com.workbuddy.matrixai.common_lib.enums.ChatEventType;

public record ChatEventResponse(
        Long id,
        ChatEventType type,
        Integer sequenceOrder,
        String content,
        String filePath,
        String metadata
) {
}
