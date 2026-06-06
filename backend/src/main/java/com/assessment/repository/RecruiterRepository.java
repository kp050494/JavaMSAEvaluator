package com.assessment.repository;

import com.assessment.model.RecruiterUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecruiterRepository extends JpaRepository<RecruiterUser, Long> {

    Optional<RecruiterUser> findByUsername(String username);
}
