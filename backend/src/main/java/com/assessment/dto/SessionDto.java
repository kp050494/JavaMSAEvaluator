package com.assessment.dto;

import java.time.Instant;
import java.util.List;

public record SessionDto(
        String id,
        String candidateName,
        String email,
        String status,
        int totalScore,
        Instant startedAt,
        Instant completedAt,
        Long durationSeconds,
        int challengesCompleted,
        List<SubmissionDto> submissions) {
}
