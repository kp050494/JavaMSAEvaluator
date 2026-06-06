package com.assessment.repository;

import com.assessment.model.CandidateSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<CandidateSession, String> {

    List<CandidateSession> findAllByOrderByStartedAtDesc();
}
