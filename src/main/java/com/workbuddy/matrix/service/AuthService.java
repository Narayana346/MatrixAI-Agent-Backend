package com.workbuddy.matrix.service;

import com.workbuddy.matrix.dto.auth.AuthResponse;
import com.workbuddy.matrix.dto.auth.LoginRequest;
import com.workbuddy.matrix.dto.auth.SignUpRequest;

public interface AuthService {
    AuthResponse signup(SignUpRequest request);

    AuthResponse login(LoginRequest request);
}
