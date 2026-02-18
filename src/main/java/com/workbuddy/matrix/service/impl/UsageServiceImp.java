package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.subscription.PlanLimitsResponse;
import com.workbuddy.matrix.dto.subscription.UsageTodayResponse;
import com.workbuddy.matrix.entity.UsageLog;
import com.workbuddy.matrix.repository.UsageLogRepository;
import com.workbuddy.matrix.repository.UserRepository;
import com.workbuddy.matrix.service.UsageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class UsageServiceImp implements UsageService {

    UsageLogRepository usageLogRepository;

    @Override
    public UsageTodayResponse getTodayUsageOfUser(Long userId) {
        UsageLog usageLog = usageLogRepository.findByUserId(userId).orElseThrow();
        return new UsageTodayResponse(usageLog.getTokenUsed(),usageLog.getDurationMs(),4,1);
    }

    @Override
    public PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userid) {
        return null;
    }

    @Override
    public void recordTokenUsage(Long id, int totalTokens) {

    }
}
