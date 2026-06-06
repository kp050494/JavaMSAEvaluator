package com.assessment.service;

import com.assessment.dto.Judge0Response;
import com.assessment.model.Challenge;
import com.assessment.model.TestCaseResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Real execution: package the candidate project, run it in the Judge0 sandbox
 * (Maven + JUnit), and parse the results. Active when execution.mode=judge0
 * (the default).
 */
@Service
@ConditionalOnProperty(name = "execution.mode", havingValue = "judge0", matchIfMissing = true)
public class Judge0Executor implements SubmissionExecutor {

    private final CodeInjectionService codeInjectionService;
    private final Judge0Service judge0Service;

    public Judge0Executor(CodeInjectionService codeInjectionService, Judge0Service judge0Service) {
        this.codeInjectionService = codeInjectionService;
        this.judge0Service = judge0Service;
    }

    @Override
    public String runningMessage() {
        return "Running the test suite (this can take ~60-90s)...";
    }

    @Override
    public ExecutionResult run(Challenge challenge, String code) {
        String projectZip = codeInjectionService.buildBase64ProjectZip(challenge, code);
        Judge0Response response = judge0Service.execute(projectZip);
        String buildLog = judge0Service.collectLog(response);
        List<TestCaseResult> results = judge0Service.parseResults(buildLog);
        return new ExecutionResult(results, buildLog);
    }
}
