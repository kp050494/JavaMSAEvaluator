package com.assessment.service;

import com.assessment.dto.ChallengeDetailDto;
import com.assessment.dto.ChallengeSummaryDto;
import com.assessment.exception.NotFoundException;
import com.assessment.model.Challenge;
import com.assessment.repository.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    public List<ChallengeSummaryDto> listAll() {
        return challengeRepository.findAllByOrderByOrderIndexAsc().stream()
                .map(ChallengeService::toSummary)
                .toList();
    }

    public ChallengeDetailDto getDetail(Long id) {
        return toDetail(getEntity(id));
    }

    public Challenge getEntity(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Challenge not found: " + id));
    }

    public long count() {
        return challengeRepository.count();
    }

    static ChallengeSummaryDto toSummary(Challenge c) {
        return new ChallengeSummaryDto(
                c.getId(), c.getSlug(), c.getOrderIndex(), c.getTitle(),
                c.getDifficulty().name(), c.getCategory(), c.getTotalTests(), c.getEstimatedMinutes());
    }

    static ChallengeDetailDto toDetail(Challenge c) {
        return new ChallengeDetailDto(
                c.getId(), c.getSlug(), c.getOrderIndex(), c.getTitle(),
                c.getDifficulty().name(), c.getCategory(), c.getDescription(), c.getStarterCode(),
                c.getConcepts(), c.getHints(), c.getTestCases(), c.getTotalTests(), c.getEstimatedMinutes());
    }
}
