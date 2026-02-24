package com.workbuddy.matrix.service;

import com.workbuddy.matrix.dto.subscription.PlanLimitsResponse;
import com.workbuddy.matrix.dto.subscription.UsageTodayResponse;

public interface UsageService {

    PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userid);
    void recordTokenUsage(Long userId, int totalTokens);
    void checkDailyTokensUsage();
}
