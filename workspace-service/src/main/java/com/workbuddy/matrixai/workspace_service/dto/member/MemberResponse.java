package com.workbuddy.matrixai.workspace_service.dto.member;


import com.workbuddy.matrixai.common_lib.enums.ProjectRole;

import java.time.Instant;

public record MemberResponse(
        Long userId,
        String username,
        String name,
        ProjectRole projectRole,
        Instant invitedAt
) {
}
