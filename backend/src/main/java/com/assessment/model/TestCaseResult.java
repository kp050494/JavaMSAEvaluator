package com.assessment.model;

/**
 * Lightweight value object for a single parsed test outcome. Produced by the
 * Judge0 log parser and streamed over WebSocket / persisted as a
 * {@link SubmissionResult}.
 */
public record TestCaseResult(String testName, boolean passed, String message) {
}
