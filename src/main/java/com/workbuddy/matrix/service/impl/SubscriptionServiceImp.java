package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.subscription.*;
import com.workbuddy.matrix.entity.Plan;
import com.workbuddy.matrix.entity.Subscription;
import com.workbuddy.matrix.repository.SubscriptionRepository;
import com.workbuddy.matrix.security.AuthUtil;
import com.workbuddy.matrix.service.SubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class SubscriptionServiceImp implements SubscriptionService {

    SubscriptionRepository subscriptionRepository;
    AuthUtil authUtil;

    @Override
    public SubscriptionResponse getCurrentSubscription() {
        Long userId = authUtil.getCurrentUserId();
        Subscription subscription = subscriptionRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Subscription not found"));
        Plan plan = subscription.getPlan();
        return new SubscriptionResponse(new PlanResponse(plan.getId(), plan.getName(), plan.getMaxProject(), plan.getMaxTokenPerDay(), plan.getUnlimitedAi(), plan.getStripePriceId()),
                subscription.getStatus().name(),Instant.now(),4L);
    }

    @Override
    public CheckOutResponse createCheckOutSessionUrl(CheckOutRequest request) {
        return null;
    }

    @Override
    public PortalResponse openCustomerPortal() {
        return null;
    }
}
