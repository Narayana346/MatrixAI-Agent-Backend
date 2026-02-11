package com.workbuddy.matrix.dto.auth;

public record AuthResponse(
        String token,
        UserProfileResponse user

) {
}
