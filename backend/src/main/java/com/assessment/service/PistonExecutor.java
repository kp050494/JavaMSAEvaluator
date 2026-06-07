package com.assessment.service;

import com.assessment.dto.PistonResponse;
import com.assessment.model.Challenge;
import com.assessment.model.TestCaseResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Real execution via a Piston instance (self-hosted, or any open execute endpoint
 * configured by PISTON_EXECUTE_URL). Active when execution.mode=piston.
 * The public emkc.org Piston API became whitelist-only in 2026, so the default
 * provider is Paiza ({@link PaizaExecutor}); use this for a self-hosted Piston.
 */
@Service
@ConditionalOnProperty(name = "execution.mode", havingValue = "piston")
public class PistonExecutor implements SubmissionExecutor {

    private final HarnessSupport harness;
    private final PistonService pistonService;

    public PistonExecutor(HarnessSupport harness, PistonService pistonService) {
        this.harness = harness;
        this.pistonService = pistonService;
    }

    @Override
    public String runningMessage() {
        return "Compiling and running your code...";
    }

    @Override
    public ExecutionResult run(Challenge challenge, String code) {
        String assembled = harness.assemble(challenge.getSlug(), code);
        PistonResponse response = pistonService.execute("Main.java", assembled);
        String log = pistonService.collectLog(response);
        if (response.compileFailed()) {
            return new ExecutionResult(List.of(), "COMPILATION FAILED\n\n" + log);
        }
        String stdout = response.run() != null ? response.run().stdout() : "";
        List<TestCaseResult> results = harness.parseMarkers(stdout);
        return new ExecutionResult(results, log);
    }
}
