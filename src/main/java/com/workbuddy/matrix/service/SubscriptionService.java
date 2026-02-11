package com.workbuddy.matrix.service;

import com.workbuddy.matrix.dto.subscription.CheckOutRequest;
import com.workbuddy.matrix.dto.subscription.CheckOutResponse;
import com.workbuddy.matrix.dto.subscription.PortalResponse;
import com.workbuddy.matrix.dto.subscription.SubscriptionResponse;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription(Long userId);

    CheckOutResponse createCheckOutSessionUrl(CheckOutRequest request, Long userId);

    PortalResponse openCustomerPortal(Long userId);
}
