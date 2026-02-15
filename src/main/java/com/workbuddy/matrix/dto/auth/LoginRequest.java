package com.workbuddy.matrix.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record LoginRequest (
        @NotBlank String userName,
    @Size(min = 4) String password
){
}
