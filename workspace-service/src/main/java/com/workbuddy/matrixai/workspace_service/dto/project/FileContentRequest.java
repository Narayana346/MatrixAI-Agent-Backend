package com.workbuddy.matrixai.workspace_service.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FileContentRequest(
        @NotBlank String path,
        @NotNull String content
) {
}
