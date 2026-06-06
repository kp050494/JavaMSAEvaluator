package com.assessment.dto;

import java.time.Instant;
import java.util.List;

public record SubmissionResponse(
        Long submissionId,
        Long challengeId,
        String status,
        int score,
        int passed,
        int total,
        Integer elapsedSeconds,
        Instant createdAt,
        List<TestResultDto> results,
        List<String> logs) {
}
