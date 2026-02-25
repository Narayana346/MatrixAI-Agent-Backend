package com.workbuddy.matrix.mapper;

import com.workbuddy.matrix.dto.auth.SignUpRequest;
import com.workbuddy.matrix.dto.auth.UserProfileResponse;
import com.workbuddy.matrix.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(SignUpRequest request);
    UserProfileResponse toProfileResponse(User user);
}
