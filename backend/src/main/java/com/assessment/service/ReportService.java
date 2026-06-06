package com.assessment.service;

import com.assessment.dto.ChallengeScoreDto;
import com.assessment.dto.SessionReportDto;
import com.assessment.dto.SubmissionDto;
import com.assessment.model.Challenge;
import com.assessment.model.CandidateSession;
import com.assessment.model.Submission;
import com.assessment.model.SubmissionStatus;
import com.assessment.repository.ChallengeRepository;
import com.assessment.repository.SubmissionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ChallengeRepository challengeRepository;
    private final SubmissionRepository submissionRepository;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    public ReportService(ChallengeRepository challengeRepository,
                         SubmissionRepository submissionRepository,
                         SessionService sessionService,
                         ObjectMapper objectMapper) {
        this.challengeRepository = challengeRepository;
        this.submissionRepository = submissionRepository;
        this.sessionService = sessionService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public SessionReportDto buildReport(String sessionId) {
        CandidateSession session = sessionService.getEntity(sessionId);
        List<Challenge> challenges = challengeRepository.findAllByOrderByOrderIndexAsc();
        Map<Long, Submission> best = bestSubmissions(sessionId);

        List<ChallengeScoreDto> scores = new ArrayList<>();
        List<SubmissionDto> bestSubmissions = new ArrayList<>();
        int totalTests = 0;
        int passedTests = 0;
        int attempted = 0;
        int sumScore = 0;

        for (Challenge c : challenges) {
            Submission b = best.get(c.getId());
            if (b != null) {
                attempted++;
                passedTests += b.getPassedCount();
                totalTests += b.getTotalCount();
                sumScore += b.getScore();
                scores.add(new ChallengeScoreDto(c.getId(), c.getTitle(), c.getCategory(),
                        c.getDifficulty().name(), b.getScore(), b.getPassedCount(), b.getTotalCount(), true));
                bestSubmissions.add(SubmissionMapper.toDto(b));
            } else {
                totalTests += c.getTotalTests();
                scores.add(new ChallengeScoreDto(c.getId(), c.getTitle(), c.getCategory(),
                        c.getDifficulty().name(), 0, 0, c.getTotalTests(), false));
            }
        }

        int totalChallenges = challenges.size();
        int totalScore = totalChallenges > 0 ? Math.round((float) sumScore / totalChallenges) : 0;
        double passRate = totalTests > 0 ? Math.round((double) passedTests * 1000 / totalTests) / 10.0 : 0.0;
        Instant end = session.getCompletedAt() != null ? session.getCompletedAt() : Instant.now();
        long totalTimeSeconds = Duration.between(session.getStartedAt(), end).getSeconds();

        return new SessionReportDto(
                session.getId(),
                session.getCandidateName(),
                session.getEmail(),
                session.getStatus().name(),
                session.getStartedAt(),
                session.getCompletedAt(),
                totalTimeSeconds,
                totalScore,
                attempted,
                totalChallenges,
                passedTests,
                totalTests,
                passRate,
                scores,
                bestSubmissions);
    }

    @Transactional(readOnly = true)
    public String exportJson(String sessionId) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(buildReport(sessionId));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to export session report", e);
        }
    }

    /** Highest-scoring completed submission per challenge (latest on ties). */
    private Map<Long, Submission> bestSubmissions(String sessionId) {
        Map<Long, Submission> best = new HashMap<>();
        for (Submission s : submissionRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)) {
            if (s.getStatus() != SubmissionStatus.COMPLETED) {
                continue;
            }
            Long challengeId = s.getChallenge().getId();
            Submission current = best.get(challengeId);
            if (current == null || s.getScore() >= current.getScore()) {
                best.put(challengeId, s);
            }
        }
        return best;
    }
}
