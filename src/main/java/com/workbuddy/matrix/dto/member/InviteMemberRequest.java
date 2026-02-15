package com.workbuddy.matrix.dto.member;

import com.workbuddy.matrix.enums.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(
        @Email @NotNull String email,
        @NotNull ProjectRole role
) {
}
