package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.subscription.PlanResponse;
import com.workbuddy.matrix.service.PlanService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanServiceImp implements PlanService {
    @Override
    public List<PlanResponse> getAllActivePlans() {
        return List.of();
    }
}
