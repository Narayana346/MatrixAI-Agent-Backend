package com.workbuddy.matrix.controller;
import com.workbuddy.matrix.dto.subscription.*;
import com.workbuddy.matrix.service.PlanService;
import com.workbuddy.matrix.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BillingController {
    private final PlanService planService;
    private final SubscriptionService subscriptionService;


    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> getAllPlans(){
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    @GetMapping("/me/subscription")
    public ResponseEntity<SubscriptionResponse> getMySubscription(){
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription());
    }

    @PostMapping("/stripe/checkout")
    public ResponseEntity<CheckOutResponse> createCheckOutSessionUrl(@RequestBody CheckOutRequest request){
        return ResponseEntity.ok(subscriptionService.createCheckOutSessionUrl(request));
    }

    @PostMapping("/stripe/portal")
    public ResponseEntity<PortalResponse> openCustomerPortal(){
        return ResponseEntity.ok(subscriptionService.openCustomerPortal());
    }
}
