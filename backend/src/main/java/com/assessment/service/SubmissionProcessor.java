package com.assessment.service;

import com.assessment.dto.ProgressMessage;
import com.assessment.dto.SubmissionResponse;
import com.assessment.dto.Judge0Response;
import com.assessment.model.Challenge;
import com.assessment.model.TestCaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Runs a submission end-to-end on a background thread: package -> Judge0 ->
 * parse -> persist, streaming progress to the candidate over WebSocket.
 */
@Service
public class SubmissionProcessor {

    private static final Logger log = LoggerFactory.getLogger(SubmissionProcessor.class);

    private final ChallengeService challengeService;
    private final CodeInjectionService codeInjectionService;
    private final Judge0Service judge0Service;
    private final WebSocketService webSocketService;
    private final SubmissionService submissionService;

    public SubmissionProcessor(ChallengeService challengeService,
                               CodeInjectionService codeInjectionService,
                               Judge0Service judge0Service,
                               WebSocketService webSocketService,
                               SubmissionService submissionService) {
        this.challengeService = challengeService;
        this.codeInjectionService = codeInjectionService;
        this.judge0Service = judge0Service;
        this.webSocketService = webSocketService;
        this.submissionService = submissionService;
    }

    @Async("submissionExecutor")
    public void processAsync(Context ctx) {
        String sessionId = ctx.sessionId();
        Long id = ctx.submissionId();
        try {
            webSocketService.send(sessionId, ProgressMessage.step(
                    "COMPILING", "Packaging your project and compiling...", id));

            Challenge challenge = challengeService.getEntity(ctx.challengeId());
            String projectZip = codeInjectionService.buildBase64ProjectZip(challenge, ctx.code());

            String token = judge0Service.createSubmission(projectZip);
            submissionService.markRunning(id, token);

            webSocketService.send(sessionId, ProgressMessage.step(
                    "RUNNING_TESTS", "Running the test suite (this can take ~60-90s)...", id));

            Judge0Response response = judge0Service.awaitResult(token);
            String buildLog = judge0Service.collectLog(response);
            List<TestCaseResult> results = judge0Service.parseResults(buildLog);

            for (TestCaseResult r : results) {
                String msg = r.passed() ? "passed" : (r.message() == null ? "failed" : r.message());
                webSocketService.send(sessionId, ProgressMessage.testResult(id, r.testName(), r.passed(), msg));
            }

            SubmissionResponse done = submissionService.complete(id, results, buildLog);
            webSocketService.send(sessionId, ProgressMessage.complete(
                    id, done.score(), done.passed(), done.total()));
        } catch (Exception e) {
            log.error("Submission {} failed", id, e);
            submissionService.markError(id, e.getMessage(), null);
            webSocketService.send(sessionId, ProgressMessage.step(
                    "ERROR", "Execution failed: " + e.getMessage(), id));
        }
    }

    /** Minimal data the background thread needs; avoids passing detached entities. */
    public record Context(Long submissionId, String sessionId, Long challengeId, String code) {
    }
}
