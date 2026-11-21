package com.workbuddy.matrixai.account_service.dto.subscription;

import com.workbuddy.matrixai.common_lib.dto.PlanDto;

import java.time.Instant;

public record SubscriptionResponse(
        PlanDto plan,
        String status,
        Instant currentPeriodEnd,
        Long tokensUsedThisCycle
) {
}
