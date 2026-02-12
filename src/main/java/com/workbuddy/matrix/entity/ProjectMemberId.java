package com.workbuddy.matrix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class ProjectMemberId implements Serializable {

    @Column(name = "project_id")
    Long projectId;
    @Column(name = "user_id")
    Long userId;
}
