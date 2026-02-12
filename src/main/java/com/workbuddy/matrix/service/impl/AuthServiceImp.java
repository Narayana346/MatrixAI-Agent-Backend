package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.auth.AuthResponse;
import com.workbuddy.matrix.dto.auth.LoginRequest;
import com.workbuddy.matrix.dto.auth.SignUpRequest;
import com.workbuddy.matrix.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImp implements AuthService {
    @Override
    public AuthResponse signup(SignUpRequest request) {
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }
}
