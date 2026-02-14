package com.workbuddy.matrix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class ProjectMemberId implements Serializable {

    @Column(name = "project_id")
    Long projectId;
    @Column(name = "user_id")
    Long userId;
}
