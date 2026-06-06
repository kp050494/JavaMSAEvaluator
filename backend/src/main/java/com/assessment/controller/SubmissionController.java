package com.assessment.controller;

import com.assessment.dto.SubmissionResponse;
import com.assessment.dto.SubmitCodeRequest;
import com.assessment.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    /** Accepts the code, starts asynchronous execution, and returns the PENDING submission. */
    @PostMapping
    public ResponseEntity<SubmissionResponse> submit(@Valid @RequestBody SubmitCodeRequest request) {
        SubmissionResponse response = submissionService.submitAndStart(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    public SubmissionResponse get(@PathVariable Long id) {
        return submissionService.getResponse(id);
    }
}
