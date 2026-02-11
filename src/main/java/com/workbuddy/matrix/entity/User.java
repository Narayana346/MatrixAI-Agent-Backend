package com.workbuddy.matrix.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String email;
    String userName;
    String password;
    String name;

    String avatarUrl;
    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;

    Instant deletedAt; // soft delete


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Subscription> subscriptions;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<UsageLog> usageLogs;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ProjectOwnership> projectOwnerships;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ProjectMember> projectMemberships;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ChatSession> chatSessions;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<ProjectFile> filesCreated;

    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    private List<ProjectFile> filesUpdated;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ChatMessage> messages;
}
