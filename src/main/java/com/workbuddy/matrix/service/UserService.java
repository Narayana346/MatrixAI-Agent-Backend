package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.dto.auth.UserProfileResponse;

public interface UserService {
    UserProfileResponse getProfile(Long userId);
}
