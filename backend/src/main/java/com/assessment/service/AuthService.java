package com.assessment.service;

import com.assessment.dto.AuthResponse;
import com.assessment.model.CandidateSession;
import com.assessment.model.RecruiterUser;
import com.assessment.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final SessionService sessionService;
    private final RecruiterService recruiterService;
    private final JwtUtil jwtUtil;

    public AuthService(SessionService sessionService,
                       RecruiterService recruiterService,
                       JwtUtil jwtUtil) {
        this.sessionService = sessionService;
        this.recruiterService = recruiterService;
        this.jwtUtil = jwtUtil;
    }

    /** Starts a new candidate session and returns a session-scoped token. */
    public AuthResponse candidateLogin(String name, String email) {
        CandidateSession session = sessionService.create(name, email);
        String token = jwtUtil.generateToken(session.getId(), Map.of(
                JwtUtil.CLAIM_ROLE, "CANDIDATE",
                JwtUtil.CLAIM_NAME, name));
        return new AuthResponse(token, session.getId(), name, "CANDIDATE");
    }

    public AuthResponse recruiterLogin(String username, String password) {
        RecruiterUser user = recruiterService.authenticate(username, password);
        String token = jwtUtil.generateToken(user.getUsername(), Map.of(
                JwtUtil.CLAIM_ROLE, "RECRUITER",
                JwtUtil.CLAIM_NAME, user.getUsername()));
        return new AuthResponse(token, null, user.getUsername(), user.getRole());
    }
}
