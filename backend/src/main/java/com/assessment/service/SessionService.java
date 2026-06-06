package com.assessment.service;

import com.assessment.dto.SessionDto;
import com.assessment.dto.SubmissionDto;
import com.assessment.exception.NotFoundException;
import com.assessment.model.CandidateSession;
import com.assessment.model.SessionStatus;
import com.assessment.model.Submission;
import com.assessment.model.SubmissionStatus;
import com.assessment.repository.ChallengeRepository;
import com.assessment.repository.SessionRepository;
import com.assessment.repository.SubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SubmissionRepository submissionRepository;
    private final ChallengeRepository challengeRepository;

    public SessionService(SessionRepository sessionRepository,
                          SubmissionRepository submissionRepository,
                          ChallengeRepository challengeRepository) {
        this.sessionRepository = sessionRepository;
        this.submissionRepository = submissionRepository;
        this.challengeRepository = challengeRepository;
    }

    @Transactional
    public CandidateSession create(String name, String email) {
        CandidateSession session = new CandidateSession();
        session.setId(UUID.randomUUID().toString());
        session.setCandidateName(name);
        session.setEmail(email);
        session.setStatus(SessionStatus.IN_PROGRESS);
        session.setStartedAt(Instant.now());
        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public CandidateSession getEntity(String id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Session not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<CandidateSession> listAll() {
        return sessionRepository.findAllByOrderByStartedAtDesc();
    }

    /** Recompute and persist the session's overall score (0-100, averaged across all challenges). */
    @Transactional
    public int recomputeScore(String sessionId) {
        CandidateSession session = getEntity(sessionId);
        int score = computeOverallScore(sessionId);
        session.setTotalScore(score);
        sessionRepository.save(session);
        return score;
    }

    @Transactional
    public CandidateSession complete(String sessionId) {
        CandidateSession session = getEntity(sessionId);
        session.setTotalScore(computeOverallScore(sessionId));
        session.setStatus(SessionStatus.COMPLETED);
        session.setCompletedAt(Instant.now());
        return sessionRepository.save(session);
    }

    /** Average of the best score per challenge over the full challenge catalogue. */
    int computeOverallScore(String sessionId) {
        long totalChallenges = challengeRepository.count();
        if (totalChallenges == 0) {
            return 0;
        }
        int sumOfBest = bestScorePerChallenge(sessionId).values().stream().mapToInt(Integer::intValue).sum();
        return Math.round((float) sumOfBest / totalChallenges);
    }

    Map<Long, Integer> bestScorePerChallenge(String sessionId) {
        return submissionRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .filter(s -> s.getStatus() == SubmissionStatus.COMPLETED)
                .collect(Collectors.toMap(
                        s -> s.getChallenge().getId(),
                        Submission::getScore,
                        Math::max));
    }

    @Transactional(readOnly = true)
    public SessionDto toDto(CandidateSession session) {
        List<Submission> submissions = submissionRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        List<SubmissionDto> submissionDtos = submissions.stream().map(SubmissionMapper::toDto).toList();
        int challengesCompleted = (int) bestScorePerChallenge(session.getId()).size();
        Long duration = session.getCompletedAt() == null
                ? null
                : Duration.between(session.getStartedAt(), session.getCompletedAt()).getSeconds();
        return new SessionDto(
                session.getId(),
                session.getCandidateName(),
                session.getEmail(),
                session.getStatus().name(),
                session.getTotalScore(),
                session.getStartedAt(),
                session.getCompletedAt(),
                duration,
                challengesCompleted,
                submissionDtos);
    }
}
