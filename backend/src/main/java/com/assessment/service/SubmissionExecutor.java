package com.assessment.service;

import com.assessment.model.Challenge;
import com.assessment.model.TestCaseResult;

import java.util.List;

/**
 * Strategy for turning a candidate submission into test results. Two
 * implementations are selected by the {@code execution.mode} property:
 *   - {@link Judge0Executor} ("judge0", default) runs the real Maven/JUnit suite
 *     inside the Judge0 sandbox;
 *   - {@link DemoExecutor} ("demo") grades heuristically, for free hosting where
 *     a privileged sandbox isn't available.
 */
public interface SubmissionExecutor {

    /** Message shown while the submission is being graded. */
    String runningMessage();

    /** Run/grade the candidate code and return the per-test outcomes plus a log. */
    ExecutionResult run(Challenge challenge, String code);

    record ExecutionResult(List<TestCaseResult> results, String log) {
    }
}
