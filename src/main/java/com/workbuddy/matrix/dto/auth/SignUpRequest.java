package com.workbuddy.matrix.dto.auth;

public record SignUpRequest(
        String email,
        String name,
        String password
) {
}
