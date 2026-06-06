package com.assessment.dto;

public record TestResultDto(
        String testName,
        boolean passed,
        String message) {
}
