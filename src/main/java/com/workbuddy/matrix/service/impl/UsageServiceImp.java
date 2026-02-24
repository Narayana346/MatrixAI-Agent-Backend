package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.subscription.PlanLimitsResponse;
import com.workbuddy.matrix.dto.subscription.PlanResponse;
import com.workbuddy.matrix.dto.subscription.SubscriptionResponse;
import com.workbuddy.matrix.dto.subscription.UsageTodayResponse;
import com.workbuddy.matrix.entity.UsageLog;
import com.workbuddy.matrix.repository.UsageLogRepository;
import com.workbuddy.matrix.repository.UserRepository;
import com.workbuddy.matrix.security.AuthUtil;
import com.workbuddy.matrix.service.SubscriptionService;
import com.workbuddy.matrix.service.UsageService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class UsageServiceImp implements UsageService {

    UsageLogRepository usageLogRepository;
    SubscriptionService subscriptionService;
    AuthUtil authUtil;


    @Override
    public PlanLimitsResponse getCurrentSubscriptionLimitsOfUser(Long userid) {
        return null;
    }

    @Override
    public void recordTokenUsage(Long userId, int actualTokens) {
        LocalDate today = LocalDate.now();

        UsageLog todayLog = usageLogRepository.findByUserIdAndDate(userId, today).
                orElseGet(() -> createNewDailyLog(userId, today));

        todayLog.setTokensUsed(todayLog.getTokensUsed() + actualTokens);
        usageLogRepository.save(todayLog);

    }

    @Override
    public void checkDailyTokensUsage() {
//        Long userId = authUtil.getCurrentUserId();
//        SubscriptionResponse subscriptionResponse = subscriptionService.getCurrentSubscription();
//        PlanResponse plan = subscriptionResponse.plan();
//
//        LocalDate today = LocalDate.now();
//
//        UsageLog todayLog = usageLogRepository.findByUserIdAndDate(userId, today).
//                orElseGet(() -> createNewDailyLog(userId, today));
//
//        if(plan.unlimitedAi()) return;
//
//        int currentUsage = todayLog.getTokensUsed();
//        int limit = plan.maxTokensPerDay();
//
//        if(currentUsage >=  limit) {
//            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
//                    "Daily limit reached, Upgrade now");
//        }

    }


    private UsageLog createNewDailyLog(Long userId, LocalDate date){

        return UsageLog.builder()
                .userId(userId)
                .tokensUsed(0)
                .date(date)
                .build();
    }
}
