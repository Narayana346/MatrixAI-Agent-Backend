package com.workbuddy.matrix.dto.member;

import com.workbuddy.matrix.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(@NotNull ProjectRole role) {
}
