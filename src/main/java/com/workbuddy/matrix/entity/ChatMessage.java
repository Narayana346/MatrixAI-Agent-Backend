package com.workbuddy.matrix.entity;

import com.workbuddy.matrix.enums.MessageRole;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    Long id;

    ChartSession chartSession;

    String content;

    String toolCalls; // JSON Array Of Tools Called

    Integer tokenUsed;

    MessageRole role;

    Instant createdAt;
}
