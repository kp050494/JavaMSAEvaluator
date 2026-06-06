package com.assessment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Real-time message streamed to /topic/submission/{sessionId} while a
 * submission is being compiled and tested.
 *
 * step is one of: CONNECTED, COMPILING, RUNNING_TESTS, TEST_RESULT, COMPLETE, ERROR
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProgressMessage(
        String step,
        String message,
        Long submissionId,
        String testName,
        Boolean passed,
        Integer score,
        Integer passedCount,
        Integer total,
        Instant timestamp) {

    public static ProgressMessage step(String step, String message, Long submissionId) {
        return new ProgressMessage(step, message, submissionId, null, null, null, null, null, Instant.now());
    }

    public static ProgressMessage testResult(Long submissionId, String testName, boolean passed, String message) {
        return new ProgressMessage("TEST_RESULT", message, submissionId, testName, passed, null, null, null, Instant.now());
    }

    public static ProgressMessage complete(Long submissionId, int score, int passed, int total) {
        return new ProgressMessage("COMPLETE", "Assessment complete", submissionId, null, null, score, passed, total, Instant.now());
    }
}
