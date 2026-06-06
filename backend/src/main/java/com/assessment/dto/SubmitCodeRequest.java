package com.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitCodeRequest(
        @NotNull(message = "challengeId is required") Long challengeId,
        @NotBlank(message = "code is required") String code,
        @NotBlank(message = "sessionId is required") String sessionId,
        Integer elapsedSeconds) {
}
