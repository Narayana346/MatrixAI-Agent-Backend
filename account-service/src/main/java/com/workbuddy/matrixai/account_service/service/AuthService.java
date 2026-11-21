package com.workbuddy.matrixai.account_service.service;


import com.workbuddy.matrixai.account_service.dto.auth.AuthResponse;
import com.workbuddy.matrixai.account_service.dto.auth.LoginRequest;
import com.workbuddy.matrixai.account_service.dto.auth.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}
