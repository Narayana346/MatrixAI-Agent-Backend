package com.workbuddy.matrix.entity;

import com.workbuddy.matrix.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "project_id",referencedColumnName = "project_id",nullable = false),
            @JoinColumn(name = "user_id",referencedColumnName = "user_id",nullable = false)
    })
    ChatSession chatSession;

    @Column(columnDefinition = "text")
    String content; // null unless User role

    String toolCalls; // JSON Array Of Tools Called

    Integer tokenUsed = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MessageRole role;  // User or ASSISTANT

    @OneToMany(mappedBy = "chatMessage",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    List<ChatEvent> events; // null unless ASSISTANT role

    @CreationTimestamp
    Instant createdAt;
}
