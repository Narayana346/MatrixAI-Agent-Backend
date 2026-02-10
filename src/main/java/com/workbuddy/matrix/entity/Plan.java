package com.workbuddy.matrix.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "plan")
public class Plan {
    @Id
    @Generated
    Long id;
    String name;

    String stripePriceId;
    Integer maxProject;
    Integer maxTokenPerDay;
    Integer maxPreview;
    Boolean unlimitedAi;
    Boolean active;
}
