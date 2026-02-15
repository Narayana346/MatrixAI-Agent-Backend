package com.workbuddy.matrix.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @Email @NotBlank String email,
        @NotBlank String userName,
        @Size(min = 1,max = 30) String name,
        @Size(min = 4) String password
) {
}
