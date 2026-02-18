package com.workbuddy.matrix.dto.chat;

import com.workbuddy.matrix.enums.ChatEventType;
import jakarta.persistence.*;

public record ChatEventResponse(

        Long id,
        ChatEventType eventType,
        String content,
        Integer sequenceOrder,
        String filePath,
        String metadata
) {
}
