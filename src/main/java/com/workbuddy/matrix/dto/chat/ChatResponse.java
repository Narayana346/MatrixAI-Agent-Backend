package com.workbuddy.matrix.dto.chat;

import com.workbuddy.matrix.entity.ChatEvent;
import com.workbuddy.matrix.entity.ChatSession;
import com.workbuddy.matrix.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        Long id,
        MessageRole role,
        String content,
        Integer tokenUsed,
        Instant createdAt,
        List<ChatEventResponse> events
) {
}
