package com.workbuddy.matrix.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workbuddy.matrix.enums.ProjectRole;
import jakarta.persistence.*;
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
@Table(name = "project_member")
public class ProjectMember {

    @EmbeddedId
    ProjectMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ProjectRole role;

    Instant invitedAt;

    Instant acceptedAt;
}
