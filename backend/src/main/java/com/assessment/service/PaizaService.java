package com.assessment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Client for the Paiza.io code-execution API (open, no signup with api_key=guest).
 * Async flow: create -> poll get_status -> get_details. Compiles + runs a single
 * Java file (public class Main) and returns its stdout for marker parsing.
 */
@Service
public class PaizaService {

    private static final Logger log = LoggerFactory.getLogger(PaizaService.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final String language;
    private final int pollTimeoutSeconds;
    private final long pollIntervalMs;

    public PaizaService(RestTemplate pistonRestTemplate,
                        @Value("${paiza.base-url:https://api.paiza.io}") String baseUrl,
                        @Value("${paiza.api-key:guest}") String apiKey,
                        @Value("${paiza.language:java}") String language,
                        @Value("${paiza.poll-timeout-seconds:60}") int pollTimeoutSeconds,
                        @Value("${paiza.poll-interval-ms:1500}") long pollIntervalMs) {
        this.restTemplate = pistonRestTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.language = language;
        this.pollTimeoutSeconds = pollTimeoutSeconds;
        this.pollIntervalMs = pollIntervalMs;
    }

    public record Output(String stdout, boolean compileFailed, String log) {
    }

    @SuppressWarnings("unchecked")
    public Output execute(String content) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("source_code", content);
        form.add("language", language);
        form.add("input", "");
        form.add("api_key", apiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        log.info("Paiza create -> {} ({}, {} bytes)", baseUrl, language, content.length());
        Map<String, Object> created = restTemplate.postForObject(
                baseUrl + "/runners/create", new HttpEntity<>(form, headers), Map.class);
        if (created == null || created.get("id") == null) {
            throw new IllegalStateException("Paiza did not return a runner id");
        }
        String id = String.valueOf(created.get("id"));

        long deadline = System.currentTimeMillis() + pollTimeoutSeconds * 1000L;
        String status = String.valueOf(created.getOrDefault("status", "running"));
        while (!"completed".equals(status) && System.currentTimeMillis() < deadline) {
            sleep(pollIntervalMs);
            Map<String, Object> st = restTemplate.getForObject(
                    statusUrl("/runners/get_status", id), Map.class);
            status = st == null ? "running" : String.valueOf(st.get("status"));
        }
        if (!"completed".equals(status)) {
            throw new IllegalStateException("Paiza run " + id + " did not complete in time");
        }

        Map<String, Object> details = restTemplate.getForObject(
                statusUrl("/runners/get_details", id), Map.class);
        if (details == null) {
            throw new IllegalStateException("Paiza returned no details for " + id);
        }

        String buildResult = str(details.get("build_result"));
        String buildErr = str(details.get("build_stderr"));
        String stdout = str(details.get("stdout"));
        String stderr = str(details.get("stderr"));
        boolean compileFailed = "failure".equals(buildResult);

        StringBuilder logBlob = new StringBuilder();
        if (!buildErr.isBlank()) {
            logBlob.append("--- compile ---\n").append(buildErr.trim()).append('\n');
        }
        if (!stdout.isBlank()) {
            logBlob.append("--- stdout ---\n").append(stdout.trim()).append('\n');
        }
        if (!stderr.isBlank()) {
            logBlob.append("--- stderr ---\n").append(stderr.trim()).append('\n');
        }
        return new Output(stdout, compileFailed, logBlob.toString());
    }

    private String statusUrl(String path, String id) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl + path)
                .queryParam("id", id)
                .queryParam("api_key", apiKey)
                .toUriString();
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while polling Paiza", e);
        }
    }
}
