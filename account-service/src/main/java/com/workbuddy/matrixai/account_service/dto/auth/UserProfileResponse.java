package com.workbuddy.matrixai.account_service.dto.auth;

public record UserProfileResponse(
        Long id,
        String username,
        String name
) {
}
