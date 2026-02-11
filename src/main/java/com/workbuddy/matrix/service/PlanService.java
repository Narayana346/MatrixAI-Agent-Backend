package com.workbuddy.matrix.service;

import com.workbuddy.matrix.dto.subscription.PlanResponse;

import java.util.List;

public interface PlanService {
    List<PlanResponse> getAllActivePlans();
}
