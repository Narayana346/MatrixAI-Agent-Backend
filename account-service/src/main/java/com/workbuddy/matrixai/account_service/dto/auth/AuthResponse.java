package com.workbuddy.matrixai.account_service.dto.auth;

public record AuthResponse(
        String token,
        UserProfileResponse user
) {

}
