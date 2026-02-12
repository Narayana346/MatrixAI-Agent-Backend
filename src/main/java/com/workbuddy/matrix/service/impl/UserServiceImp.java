package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.auth.UserProfileResponse;
import com.workbuddy.matrix.entity.User;
import com.workbuddy.matrix.repository.UserRepository;
import com.workbuddy.matrix.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserServiceImp implements UserService {
    UserRepository userRepository;
    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.getReferenceById(userId);
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getAvatarUrl());
    }
}
