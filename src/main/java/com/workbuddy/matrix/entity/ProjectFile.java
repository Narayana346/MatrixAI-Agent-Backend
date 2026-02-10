package com.workbuddy.matrix.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
@Table(name = "project_file")
public class ProjectFile {
    @Id
    Long id;

    @ManyToOne
    Project project;

    String path;

    String minioObjectKey;

    User createdBy;

    User updateBy;

    Instant createdAt;

    Instant updatedAt;



}
