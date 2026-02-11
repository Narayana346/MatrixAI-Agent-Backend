package com.workbuddy.matrix.controller;

import com.workbuddy.matrix.dto.subscription.PlanLimitsResponse;
import com.workbuddy.matrix.service.UsageService;
import com.workbuddy.matrix.dto.subscription.UsageTodayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageController {
    private final UsageService usageService;

    @GetMapping("/today")
    public ResponseEntity<UsageTodayResponse> getTodayUsage(){
        Long userId = 1L;
        return ResponseEntity.ok(usageService.getTodayUsageOfUser(userId));
    }

    @GetMapping("/limits")
    public ResponseEntity<PlanLimitsResponse> getPlanLimits(){
        Long userid = 1L;
        return ResponseEntity.ok(usageService.getCurrentSubscriptionLimitsOfUser(userid));
    }
}
