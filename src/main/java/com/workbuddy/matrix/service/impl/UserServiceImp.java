package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.auth.UserProfileResponse;
import com.workbuddy.matrix.entity.User;
import com.workbuddy.matrix.error.ResourceNotFoundException;
import com.workbuddy.matrix.mapper.UserMapper;
import com.workbuddy.matrix.repository.UserRepository;
import com.workbuddy.matrix.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserServiceImp implements UserService , UserDetailsService {
    UserRepository userRepository;
    UserMapper userMapper;
    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found", userId.toString()));
        return userMapper.toProfileResponse(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
