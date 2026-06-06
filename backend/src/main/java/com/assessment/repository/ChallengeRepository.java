package com.assessment.repository;

import com.assessment.model.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    List<Challenge> findAllByOrderByOrderIndexAsc();

    Optional<Challenge> findBySlug(String slug);
}
