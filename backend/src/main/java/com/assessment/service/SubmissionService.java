package com.assessment.service;

import com.assessment.dto.SubmissionResponse;
import com.assessment.dto.SubmitCodeRequest;
import com.assessment.exception.NotFoundException;
import com.assessment.model.Challenge;
import com.assessment.model.CandidateSession;
import com.assessment.model.Submission;
import com.assessment.model.SubmissionResult;
import com.assessment.model.SubmissionStatus;
import com.assessment.model.TestCaseResult;
import com.assessment.repository.SubmissionRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class SubmissionService {

    private static final int MAX_LOG_CHARS = 20_000;

    private final SubmissionRepository submissionRepository;
    private final ChallengeService challengeService;
    private final SessionService sessionService;
    private final SubmissionProcessor submissionProcessor;

    public SubmissionService(SubmissionRepository submissionRepository,
                             ChallengeService challengeService,
                             SessionService sessionService,
                             @Lazy SubmissionProcessor submissionProcessor) {
        this.submissionRepository = submissionRepository;
        this.challengeService = challengeService;
        this.sessionService = sessionService;
        this.submissionProcessor = submissionProcessor;
    }

    /** Persist a PENDING submission and kick off asynchronous execution. */
    @Transactional
    public SubmissionResponse submitAndStart(SubmitCodeRequest request) {
        CandidateSession session = sessionService.getEntity(request.sessionId());
        Challenge challenge = challengeService.getEntity(request.challengeId());

        Submission submission = new Submission();
        submission.setSession(session);
        submission.setChallenge(challenge);
        submission.setCode(request.code());
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setTotalCount(challenge.getTotalTests());
        submission.setElapsedSeconds(request.elapsedSeconds() == null ? 0 : request.elapsedSeconds());
        submission.setCreatedAt(Instant.now());
        submission = submissionRepository.save(submission);

        SubmissionProcessor.Context ctx = new SubmissionProcessor.Context(
                submission.getId(), session.getId(), challenge.getId(), request.code());
        submissionProcessor.processAsync(ctx);

        return SubmissionMapper.toResponse(submission);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getResponse(Long id) {
        return SubmissionMapper.toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public Submission getEntity(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Submission not found: " + id));
    }

    @Transactional
    public void markRunning(Long id, String judge0Token) {
        Submission s = getEntity(id);
        s.setStatus(SubmissionStatus.RUNNING);
        s.setJudge0Token(judge0Token);
        submissionRepository.save(s);
    }

    @Transactional
    public void markError(Long id, String message, String logs) {
        Submission s = getEntity(id);
        s.setStatus(SubmissionStatus.ERROR);
        s.setScore(0);
        s.setPassedCount(0);
        s.setLogs(truncate(logs == null ? message : logs));
        submissionRepository.save(s);
    }

    /** Persist parsed results, compute the score, and refresh the session score. */
    @Transactional
    public SubmissionResponse complete(Long id, List<TestCaseResult> results, String logs) {
        Submission s = getEntity(id);
        s.getResults().clear();

        int total = results.isEmpty() ? s.getChallenge().getTotalTests() : results.size();
        int passed = 0;
        for (TestCaseResult r : results) {
            if (r.passed()) {
                passed++;
            }
            s.addResult(new SubmissionResult(r.testName(), r.passed(), r.message()));
        }
        int score = total > 0 ? Math.round((float) passed * 100 / total) : 0;

        s.setPassedCount(passed);
        s.setTotalCount(total);
        s.setScore(score);
        s.setStatus(SubmissionStatus.COMPLETED);
        s.setLogs(truncate(logs));
        submissionRepository.save(s);

        sessionService.recomputeScore(s.getSession().getId());
        return SubmissionMapper.toResponse(s);
    }

    private static String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() <= MAX_LOG_CHARS ? s : s.substring(s.length() - MAX_LOG_CHARS);
    }
}
