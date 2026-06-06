package com.assessment.dto;

import java.util.List;

public record ChallengeDetailDto(
        Long id,
        String slug,
        int orderIndex,
        String title,
        String difficulty,
        String category,
        String description,
        String starterCode,
        List<String> concepts,
        List<String> hints,
        List<String> testCases,
        int totalTests,
        int estimatedMinutes) {
}
