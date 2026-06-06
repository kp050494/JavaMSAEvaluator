package com.assessment.service;

import com.assessment.dto.ProgressMessage;
import com.assessment.dto.SubmissionResponse;
import com.assessment.model.Challenge;
import com.assessment.model.TestCaseResult;
import com.assessment.service.SubmissionExecutor.ExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Runs a submission end-to-end on a background thread: grade (via the configured
 * {@link SubmissionExecutor}) -> persist, streaming progress over WebSocket.
 */
@Service
public class SubmissionProcessor {

    private static final Logger log = LoggerFactory.getLogger(SubmissionProcessor.class);

    private final ChallengeService challengeService;
    private final SubmissionExecutor executor;
    private final WebSocketService webSocketService;
    private final SubmissionService submissionService;

    public SubmissionProcessor(ChallengeService challengeService,
                               SubmissionExecutor executor,
                               WebSocketService webSocketService,
                               SubmissionService submissionService) {
        this.challengeService = challengeService;
        this.executor = executor;
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
            submissionService.markRunning(id, null);

            webSocketService.send(sessionId, ProgressMessage.step(
                    "RUNNING_TESTS", executor.runningMessage(), id));

            ExecutionResult execution = executor.run(challenge, ctx.code());

            for (TestCaseResult r : execution.results()) {
                String msg = r.passed() ? "passed" : (r.message() == null ? "failed" : r.message());
                webSocketService.send(sessionId, ProgressMessage.testResult(id, r.testName(), r.passed(), msg));
                sleepQuietly(120);
            }

            SubmissionResponse done = submissionService.complete(id, execution.results(), execution.log());
            webSocketService.send(sessionId, ProgressMessage.complete(
                    id, done.score(), done.passed(), done.total()));
        } catch (Exception e) {
            log.error("Submission {} failed", id, e);
            submissionService.markError(id, e.getMessage(), null);
            webSocketService.send(sessionId, ProgressMessage.step(
                    "ERROR", "Execution failed: " + e.getMessage(), id));
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Minimal data the background thread needs; avoids passing detached entities. */
    public record Context(Long submissionId, String sessionId, Long challengeId, String code) {
    }
}
