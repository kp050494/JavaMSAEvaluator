package com.assessment.controller;

import com.assessment.dto.ChallengeDetailDto;
import com.assessment.dto.ChallengeSummaryDto;
import com.assessment.service.ChallengeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public List<ChallengeSummaryDto> list() {
        return challengeService.listAll();
    }

    @GetMapping("/{id}")
    public ChallengeDetailDto detail(@PathVariable Long id) {
        return challengeService.getDetail(id);
    }
}
