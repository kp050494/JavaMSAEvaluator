package com.assessment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Public liveness endpoint (used by host health checks). */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final String executionMode;

    public HealthController(@Value("${execution.mode:judge0}") String executionMode) {
        this.executionMode = executionMode;
    }

    @GetMapping
    public Map<String, Object> health() {
        return Map.of("status", "UP", "executionMode", executionMode);
    }
}
