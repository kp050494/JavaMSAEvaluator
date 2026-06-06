package com.assessment.dto;

import java.time.Instant;
import java.util.List;

public record SubmissionDto(
        Long id,
        Long challengeId,
        String challengeTitle,
        String category,
        String difficulty,
        String status,
        int score,
        int passed,
        int total,
        int elapsedSeconds,
        Instant createdAt,
        String code,
        List<TestResultDto> results) {
}
