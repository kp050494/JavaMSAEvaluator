package com.assessment.dto;

import java.time.Instant;
import java.util.List;

public record SessionReportDto(
        String sessionId,
        String candidateName,
        String email,
        String status,
        Instant startedAt,
        Instant completedAt,
        Long totalTimeSeconds,
        int totalScore,
        int challengesAttempted,
        int totalChallenges,
        int totalTestsPassed,
        int totalTests,
        double passRate,
        List<ChallengeScoreDto> challengeScores,
        List<SubmissionDto> bestSubmissions) {
}
