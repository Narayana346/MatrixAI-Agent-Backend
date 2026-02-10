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
@Table(name = "project")
public class Project {
    @Id
    Long id;

    String name;

    User owner;

    Boolean isPublic = false;

    Instant createdAt;

    Instant updatedAt;

    Instant deletedAt; // soft delete

}
