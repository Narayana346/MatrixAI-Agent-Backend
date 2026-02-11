package com.workbuddy.matrix.service;

import com.workbuddy.matrix.dto.auth.UserProfileResponse;

public interface UserService {
    UserProfileResponse getProfile(Long userId);
}
