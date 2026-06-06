package com.assessment.dto;

public record ChallengeSummaryDto(
        Long id,
        String slug,
        int orderIndex,
        String title,
        String difficulty,
        String category,
        int totalTests,
        int estimatedMinutes) {
}
