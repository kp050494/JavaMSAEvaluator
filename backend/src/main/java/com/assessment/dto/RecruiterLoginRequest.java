package com.assessment.dto;

import jakarta.validation.constraints.NotBlank;

public record RecruiterLoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}
