package com.workbuddy.matrix.dto.chat;

import com.workbuddy.matrix.entity.ChatSession;
import com.workbuddy.matrix.enums.MessageRole;

import java.time.Instant;

public record ChatResponse(
        Long id,
        ChatSession chatSession,
        MessageRole role,
        String content,
        Integer tokenUsed,
        Instant createdAt,
        List<ChatEvent> events
) {
}
