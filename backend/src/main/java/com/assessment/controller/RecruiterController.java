package com.assessment.controller;

import com.assessment.dto.RecruiterSessionSummaryDto;
import com.assessment.dto.SessionDto;
import com.assessment.service.RecruiterService;
import com.assessment.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
public class RecruiterController {

    private final RecruiterService recruiterService;
    private final ReportService reportService;

    public RecruiterController(RecruiterService recruiterService, ReportService reportService) {
        this.recruiterService = recruiterService;
        this.reportService = reportService;
    }

    @GetMapping("/sessions")
    public List<RecruiterSessionSummaryDto> sessions() {
        return recruiterService.listSessionSummaries();
    }

    @GetMapping("/sessions/{id}")
    public SessionDto session(@PathVariable String id) {
        return recruiterService.getSessionDetail(id);
    }

    @GetMapping("/sessions/{id}/export")
    public ResponseEntity<String> export(@PathVariable String id) {
        String json = reportService.exportJson(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"session-" + id + ".json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }
}
