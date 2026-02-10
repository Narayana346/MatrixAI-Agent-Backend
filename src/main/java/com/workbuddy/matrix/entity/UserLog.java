package com.workbuddy.matrix.entity;

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
@Table(name = "user_log")
public class UserLog {
    @Id
    Long id;

    User user;

    Project project;

    String action;

    Integer tokenUsed;

    Integer durationMs;

    String metaData; // JSON of {model_used , prompt_used}

    Instant createdAt;
}
