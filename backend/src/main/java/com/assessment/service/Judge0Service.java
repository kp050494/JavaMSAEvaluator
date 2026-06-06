package com.assessment.service;

import com.assessment.dto.Judge0Response;
import com.assessment.model.TestCaseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST client for a self-hosted Judge0 CE instance. Submits the candidate's
 * multi-file Maven project, polls until the run finishes, and parses the
 * JUNIT_RESULT:: markers emitted by the template's test suite.
 */
@Service
public class Judge0Service {

    private static final Logger log = LoggerFactory.getLogger(Judge0Service.class);
    private static final String MARKER = "JUNIT_RESULT::";

    private final RestTemplate restTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final String baseUrl;
    private final String authToken;
    private final int languageId;
    private final int cpuTimeLimit;
    private final int wallTimeLimit;
    private final int memoryLimit;
    private final int pollTimeoutSeconds;
    private final long pollIntervalMs;

    public Judge0Service(RestTemplate judge0RestTemplate,
                         com.fasterxml.jackson.databind.ObjectMapper objectMapper,
                         @Value("${judge0.base-url}") String baseUrl,
                         @Value("${judge0.auth-token:}") String authToken,
                         @Value("${judge0.language-id:89}") int languageId,
                         @Value("${judge0.cpu-time-limit:30}") int cpuTimeLimit,
                         @Value("${judge0.wall-time-limit:120}") int wallTimeLimit,
                         @Value("${judge0.memory-limit:512000}") int memoryLimit,
                         @Value("${judge0.poll-timeout-seconds:120}") int pollTimeoutSeconds,
                         @Value("${judge0.poll-interval-ms:2000}") long pollIntervalMs) {
        this.restTemplate = judge0RestTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.authToken = authToken;
        this.languageId = languageId;
        this.cpuTimeLimit = cpuTimeLimit;
        this.wallTimeLimit = wallTimeLimit;
        this.memoryLimit = memoryLimit;
        this.pollTimeoutSeconds = pollTimeoutSeconds;
        this.pollIntervalMs = pollIntervalMs;
    }

    /** Create a submission and return its token (asynchronous execution). */
    public String createSubmission(String base64ProjectZip) {
        // Build the payload as an explicit snake_case map so the field names always
        // match Judge0's strong params regardless of Jackson record-naming behaviour.
        Map<String, Object> request = new LinkedHashMap<>();
        // Language 89 (Multi-file program) requires source_code to be blank; the
        // whole project (incl. compile/run scripts) travels in additional_files.
        request.put("source_code", "");
        request.put("language_id", languageId);
        request.put("cpu_time_limit", cpuTimeLimit);
        request.put("wall_time_limit", wallTimeLimit);
        request.put("memory_limit", memoryLimit);
        request.put("additional_files", base64ProjectZip);

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/submissions")
                .queryParam("base64_encoded", "false")
                .queryParam("wait", "false")
                .toUriString();

        String json;
        try {
            json = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise Judge0 request", e);
        }
        log.info("Judge0 submit -> {} (body {} chars, prefix: {})",
                url, json.length(), json.substring(0, Math.min(120, json.length())));

        Judge0Response created = restTemplate.postForObject(url, new HttpEntity<>(json, headers()), Judge0Response.class);
        if (created == null || created.token() == null) {
            throw new IllegalStateException("Judge0 did not return a submission token");
        }
        return created.token();
    }

    /** Poll a submission until it finishes (status id >= 3) or the timeout elapses. */
    public Judge0Response awaitResult(String token) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/submissions/" + token)
                .queryParam("base64_encoded", "false")
                .queryParam("fields", "token,stdout,stderr,compile_output,message,status,time,memory")
                .toUriString();

        long deadline = System.currentTimeMillis() + pollTimeoutSeconds * 1000L;
        Judge0Response last = null;
        while (System.currentTimeMillis() < deadline) {
            last = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET,
                    new HttpEntity<>(headers()), Judge0Response.class).getBody();
            if (last != null && last.isFinished()) {
                return last;
            }
            sleep(pollIntervalMs);
        }
        throw new IllegalStateException("Judge0 submission " + token + " did not finish within "
                + pollTimeoutSeconds + "s");
    }

    /** Convenience: create + await in one call. */
    public Judge0Response execute(String base64ProjectZip) {
        return awaitResult(createSubmission(base64ProjectZip));
    }

    /** Combine the build output streams into a single log blob. */
    public String collectLog(Judge0Response response) {
        StringBuilder sb = new StringBuilder();
        if (response.compileOutput() != null && !response.compileOutput().isBlank()) {
            sb.append(response.compileOutput());
        }
        if (response.stdout() != null) {
            sb.append(response.stdout());
        }
        if (response.stderr() != null && !response.stderr().isBlank()) {
            sb.append('\n').append(response.stderr());
        }
        return sb.toString();
    }

    /**
     * Parse the JUNIT_RESULT:: markers out of the captured build log.
     * Format: {@code JUNIT_RESULT::<testName>::PASSED}
     *         {@code JUNIT_RESULT::<testName>::FAILED::<message>}
     */
    public List<TestCaseResult> parseResults(String log) {
        List<TestCaseResult> results = new ArrayList<>();
        if (log == null || log.isEmpty()) {
            return results;
        }
        for (String raw : log.split("\\R")) {
            int idx = raw.indexOf(MARKER);
            if (idx < 0) {
                continue;
            }
            String line = raw.substring(idx + MARKER.length());
            String[] parts = line.split("::", 3);
            if (parts.length < 2) {
                continue;
            }
            String testName = parts[0].trim();
            boolean passed = "PASSED".equalsIgnoreCase(parts[1].trim());
            String message = parts.length == 3 ? parts[2].trim() : null;
            results.add(new TestCaseResult(testName, passed, message));
        }
        return results;
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null && !authToken.isBlank()) {
            headers.set("X-Auth-Token", authToken);
        }
        return headers;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while polling Judge0", e);
        }
    }
}
