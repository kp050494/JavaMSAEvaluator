package com.assessment.controller;

import com.assessment.dto.AuthResponse;
import com.assessment.dto.CandidateLoginRequest;
import com.assessment.dto.RecruiterLoginRequest;
import com.assessment.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse candidateLogin(@Valid @RequestBody CandidateLoginRequest request) {
        return authService.candidateLogin(request.name(), request.email());
    }

    @PostMapping("/recruiter/login")
    public AuthResponse recruiterLogin(@Valid @RequestBody RecruiterLoginRequest request) {
        return authService.recruiterLogin(request.username(), request.password());
    }
}
