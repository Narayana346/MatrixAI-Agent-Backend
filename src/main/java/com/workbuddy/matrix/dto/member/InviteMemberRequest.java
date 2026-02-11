package com.workbuddy.matrix.dto.member;

import com.workbuddy.matrix.enums.ProjectRole;

public record InviteMemberRequest(
        String email,
        ProjectRole role
) {
}
