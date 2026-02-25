package com.workbuddy.matrix.dto.auth;

public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String avatarUrl) {
}
