package com.workbuddy.matrixai.account_service.mapper;

import com.workbuddy.matrixai.account_service.dto.subscription.SubscriptionResponse;
import com.workbuddy.matrixai.account_service.entity.Plan;
import com.workbuddy.matrixai.account_service.entity.Subscription;
import com.workbuddy.matrixai.common_lib.dto.PlanDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponse toSubscriptionResponse(Subscription subscription);

    PlanDto toPlanResponse(Plan plan);
}
