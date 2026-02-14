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
@Table(name = "project_members")
public class ProjectMembers {

    @EmbeddedId
    ProjectMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ProjectRole role;

    Instant invitedAt;

    Instant acceptedAt;
}
