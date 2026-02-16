package com.workbuddy.matrix.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(@NotBlank String message, @NotNull Long projectId) {
}
