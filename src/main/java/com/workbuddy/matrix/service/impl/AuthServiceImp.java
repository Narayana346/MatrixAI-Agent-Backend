package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.auth.AuthResponse;
import com.workbuddy.matrix.dto.auth.LoginRequest;
import com.workbuddy.matrix.dto.auth.SignUpRequest;
import com.workbuddy.matrix.entity.User;
import com.workbuddy.matrix.error.BadRequestException;
import com.workbuddy.matrix.mapper.UserMapper;
import com.workbuddy.matrix.repository.UserRepository;
import com.workbuddy.matrix.security.AuthUtil;
import com.workbuddy.matrix.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AuthServiceImp implements AuthService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    AuthUtil authUtil;
    AuthenticationManager authenticationManager;
    @Override
    public AuthResponse signup(SignUpRequest request) {

        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new BadRequestException("User already exists " + request.name());
        });

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        String accessToken = authUtil.generateAccessToken(user);

        return new AuthResponse(accessToken, userMapper.toProfileResponse(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.userName(), request.password())
        );

        User user = (User) authentication.getPrincipal();

        String token = authUtil.generateAccessToken(user);

        return new AuthResponse(token, userMapper.toProfileResponse(user));
    }
}
