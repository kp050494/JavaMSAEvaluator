package com.assessment.service;

import com.assessment.dto.RecruiterSessionSummaryDto;
import com.assessment.dto.SessionDto;
import com.assessment.exception.UnauthorizedException;
import com.assessment.model.CandidateSession;
import com.assessment.model.RecruiterUser;
import com.assessment.repository.ChallengeRepository;
import com.assessment.repository.RecruiterRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Service
public class RecruiterService {

    private final RecruiterRepository recruiterRepository;
    private final ChallengeRepository challengeRepository;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;

    public RecruiterService(RecruiterRepository recruiterRepository,
                            ChallengeRepository challengeRepository,
                            SessionService sessionService,
                            PasswordEncoder passwordEncoder) {
        this.recruiterRepository = recruiterRepository;
        this.challengeRepository = challengeRepository;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public RecruiterUser authenticate(String username, String password) {
        RecruiterUser user = recruiterRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public List<RecruiterSessionSummaryDto> listSessionSummaries() {
        int totalChallenges = (int) challengeRepository.count();
        return sessionService.listAll().stream()
                .map(session -> toSummary(session, totalChallenges))
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionDto getSessionDetail(String sessionId) {
        return sessionService.toDto(sessionService.getEntity(sessionId));
    }

    private RecruiterSessionSummaryDto toSummary(CandidateSession session, int totalChallenges) {
        Collection<Integer> bestScores = sessionService.bestScorePerChallenge(session.getId()).values();
        int challengesCompleted = bestScores.size();
        double averageScore = bestScores.isEmpty()
                ? 0.0
                : Math.round(bestScores.stream().mapToInt(Integer::intValue).average().orElse(0) * 10) / 10.0;
        Instant end = session.getCompletedAt() != null ? session.getCompletedAt() : Instant.now();
        long durationSeconds = Duration.between(session.getStartedAt(), end).getSeconds();

        return new RecruiterSessionSummaryDto(
                session.getId(),
                session.getCandidateName(),
                session.getEmail(),
                session.getStatus().name(),
                session.getStartedAt(),
                session.getCompletedAt(),
                durationSeconds,
                challengesCompleted,
                totalChallenges,
                session.getTotalScore(),
                averageScore);
    }
}
