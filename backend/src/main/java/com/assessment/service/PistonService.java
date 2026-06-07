package com.assessment.service;

import com.assessment.dto.PistonResponse;
import com.assessment.model.TestCaseResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for a Piston execute endpoint (public emkc.org or a self-hosted instance
 * via PISTON_EXECUTE_URL). Compiles + runs a single Java file and parses the
 * TEST_PASS:: / TEST_FAIL:: markers the challenge harness prints to stdout.
 */
@Service
public class PistonService {

    private static final Logger log = LoggerFactory.getLogger(PistonService.class);
    private static final String PASS = "TEST_PASS::";
    private static final String FAIL = "TEST_FAIL::";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String executeUrl;
    private final String language;
    private final String version;
    private final int runTimeoutMs;
    private final int compileTimeoutMs;

    public PistonService(RestTemplate pistonRestTemplate,
                         ObjectMapper objectMapper,
                         @Value("${piston.execute-url}") String executeUrl,
                         @Value("${piston.language:java}") String language,
                         @Value("${piston.version:15.0.2}") String version,
                         @Value("${piston.run-timeout-ms:10000}") int runTimeoutMs,
                         @Value("${piston.compile-timeout-ms:10000}") int compileTimeoutMs) {
        this.restTemplate = pistonRestTemplate;
        this.objectMapper = objectMapper;
        this.executeUrl = executeUrl;
        this.language = language;
        this.version = version;
        this.runTimeoutMs = runTimeoutMs;
        this.compileTimeoutMs = compileTimeoutMs;
    }

    public PistonResponse execute(String fileName, String content) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("language", language);
        body.put("version", version);
        body.put("files", List.of(Map.of("name", fileName, "content", content)));
        body.put("stdin", "");
        body.put("compile_timeout", compileTimeoutMs);
        body.put("run_timeout", runTimeoutMs);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise Piston request", e);
        }

        log.info("Piston execute -> {} ({} {}, {} bytes)", executeUrl, language, version, content.length());
        PistonResponse response = restTemplate.postForObject(executeUrl, new HttpEntity<>(json, headers), PistonResponse.class);
        if (response == null) {
            throw new IllegalStateException("Piston returned an empty response");
        }
        return response;
    }

    /** Combine compile + run output into one readable log blob. */
    public String collectLog(PistonResponse response) {
        StringBuilder sb = new StringBuilder();
        if (response.compile() != null && notBlank(response.compile().output())) {
            sb.append("--- compile ---\n").append(response.compile().output().trim()).append('\n');
        }
        if (response.run() != null && notBlank(response.run().output())) {
            sb.append("--- run ---\n").append(response.run().output().trim()).append('\n');
        }
        return sb.toString();
    }

    /** Parse TEST_PASS::name and TEST_FAIL::name::message lines from program stdout. */
    public List<TestCaseResult> parseResults(String stdout) {
        List<TestCaseResult> results = new ArrayList<>();
        if (stdout == null) {
            return results;
        }
        for (String raw : stdout.split("\\R")) {
            String line = raw.trim();
            if (line.startsWith(PASS)) {
                results.add(new TestCaseResult(line.substring(PASS.length()).trim(), true, null));
            } else if (line.startsWith(FAIL)) {
                String rest = line.substring(FAIL.length());
                String[] parts = rest.split("::", 2);
                String name = parts[0].trim();
                String msg = parts.length > 1 ? parts[1].trim() : "failed";
                results.add(new TestCaseResult(name, false, msg));
            }
        }
        return results;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
