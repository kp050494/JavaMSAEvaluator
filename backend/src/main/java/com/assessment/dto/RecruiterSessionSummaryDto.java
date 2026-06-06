package com.assessment.dto;

import java.time.Instant;

public record RecruiterSessionSummaryDto(
        String id,
        String candidateName,
        String email,
        String status,
        Instant startedAt,
        Instant completedAt,
        Long durationSeconds,
        int challengesCompleted,
        int totalChallenges,
        int totalScore,
        double averageScore) {
}
