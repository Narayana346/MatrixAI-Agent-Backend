package com.workbuddy.matrix.entity;

import com.workbuddy.matrix.enums.PreviewStatus;
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
@Table(name = "preview")
public class Preview {
    @Id
    Long id;

    Project project;

    String namespace; //k8s namespace

    String podName;

    String previewUrl;

    PreviewStatus status;

    Instant startedAt;

    Instant terminatedAt;

    Instant createdAt;
}
