package com.workbuddy.matrixai.account_service.mapper;

import com.workbuddy.matrixai.account_service.dto.auth.SignupRequest;
import com.workbuddy.matrixai.account_service.dto.auth.UserProfileResponse;
import com.workbuddy.matrixai.account_service.entity.User;
import com.workbuddy.matrixai.common_lib.dto.UserDto;
import com.workbuddy.matrixai.common_lib.security.JwtUserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(SignupRequest signupRequest);

    @Mapping(source = "userId", target = "id")
    UserProfileResponse toUserProfileResponse(JwtUserPrincipal user);

    UserDto toUserDto(User user);

}
