package com.assessment.dto;

public record ChallengeScoreDto(
        Long challengeId,
        String title,
        String category,
        String difficulty,
        int score,
        int passed,
        int total,
        boolean attempted) {
}
