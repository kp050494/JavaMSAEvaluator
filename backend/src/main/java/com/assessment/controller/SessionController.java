package com.assessment.controller;

import com.assessment.dto.SessionDto;
import com.assessment.dto.SessionReportDto;
import com.assessment.service.ReportService;
import com.assessment.service.SessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final ReportService reportService;

    public SessionController(SessionService sessionService, ReportService reportService) {
        this.sessionService = sessionService;
        this.reportService = reportService;
    }

    @GetMapping("/{sessionId}")
    public SessionDto get(@PathVariable String sessionId) {
        return sessionService.toDto(sessionService.getEntity(sessionId));
    }

    @GetMapping("/{sessionId}/report")
    public SessionReportDto report(@PathVariable String sessionId) {
        return reportService.buildReport(sessionId);
    }

    @PostMapping("/{sessionId}/complete")
    public SessionReportDto complete(@PathVariable String sessionId) {
        sessionService.complete(sessionId);
        return reportService.buildReport(sessionId);
    }
}
