package com.assessment.service;

import com.assessment.model.Challenge;
import com.assessment.model.TestCaseResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Real execution via the Paiza.io API (open, no signup). Assembles the single-file
 * harness with the candidate code, compiles + runs it, and parses TEST markers.
 * Active when execution.mode=paiza (the default).
 */
@Service
@ConditionalOnProperty(name = "execution.mode", havingValue = "paiza", matchIfMissing = true)
public class PaizaExecutor implements SubmissionExecutor {

    private final HarnessSupport harness;
    private final PaizaService paiza;

    public PaizaExecutor(HarnessSupport harness, PaizaService paiza) {
        this.harness = harness;
        this.paiza = paiza;
    }

    @Override
    public String runningMessage() {
        return "Compiling and running your code...";
    }

    @Override
    public ExecutionResult run(Challenge challenge, String code) {
        String assembled = harness.assemble(challenge.getSlug(), code);
        PaizaService.Output out = paiza.execute(assembled);
        if (out.compileFailed()) {
            return new ExecutionResult(List.of(), "COMPILATION FAILED\n\n" + out.log());
        }
        List<TestCaseResult> results = harness.parseMarkers(out.stdout());
        return new ExecutionResult(results, out.log());
    }
}
