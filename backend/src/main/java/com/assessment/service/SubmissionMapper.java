package com.assessment.service;

import com.assessment.dto.SubmissionDto;
import com.assessment.dto.SubmissionResponse;
import com.assessment.dto.TestResultDto;
import com.assessment.model.Challenge;
import com.assessment.model.Submission;

import java.util.Arrays;
import java.util.List;

/** Shared mapping helpers between Submission entities and their DTOs. */
final class SubmissionMapper {

    private SubmissionMapper() {
    }

    static List<TestResultDto> results(Submission s) {
        return s.getResults().stream()
                .map(r -> new TestResultDto(r.getTestName(), r.isPassed(), r.getMessage()))
                .toList();
    }

    static List<String> logLines(String logs) {
        if (logs == null || logs.isBlank()) {
            return List.of();
        }
        return Arrays.stream(logs.split("\\R")).toList();
    }

    static SubmissionResponse toResponse(Submission s) {
        return new SubmissionResponse(
                s.getId(),
                s.getChallenge().getId(),
                s.getStatus().name(),
                s.getScore(),
                s.getPassedCount(),
                s.getTotalCount(),
                s.getElapsedSeconds(),
                s.getCreatedAt(),
                results(s),
                logLines(s.getLogs()));
    }

    static SubmissionDto toDto(Submission s) {
        Challenge c = s.getChallenge();
        return new SubmissionDto(
                s.getId(),
                c.getId(),
                c.getTitle(),
                c.getCategory(),
                c.getDifficulty().name(),
                s.getStatus().name(),
                s.getScore(),
                s.getPassedCount(),
                s.getTotalCount(),
                s.getElapsedSeconds(),
                s.getCreatedAt(),
                s.getCode(),
                results(s));
    }
}
