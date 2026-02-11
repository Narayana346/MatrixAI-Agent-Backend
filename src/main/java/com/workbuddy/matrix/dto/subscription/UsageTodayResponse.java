package com.workbuddy.matrix.dto.subscription;

public record UsageTodayResponse(
        int tokenUsed,
        int tokenLimits,
        int previewsRunning,
        int previewsLimit
) {
}
