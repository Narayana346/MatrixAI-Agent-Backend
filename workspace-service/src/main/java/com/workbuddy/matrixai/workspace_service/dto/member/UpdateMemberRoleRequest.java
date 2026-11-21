package com.workbuddy.matrixai.workspace_service.dto.member;

import com.workbuddy.matrixai.common_lib.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(
        @NotNull ProjectRole role) {
}
