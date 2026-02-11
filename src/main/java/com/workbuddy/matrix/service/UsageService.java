package com.workbuddy.matrix.service;

import com.workbuddy.matrix.dto.subscription.PlanLimitsResponse;
import com.workbuddy.matrix.dto.subscription.UsageTodayResponse;

public interface UsageService {
    UsageTodayResponse getTodayUsageOfUser(Long userId);

    PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userid);
}
