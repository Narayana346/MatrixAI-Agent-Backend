package com.workbuddy.matrix.dto.subscription;

public record PlanLimitsResponse(
        String planName,
        int maxTokenPerDay,
        int maxProjects,
        boolean unlimitedAi
) {
}
