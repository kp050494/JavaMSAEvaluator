package com.assessment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Submission payload for Judge0 CE. For the multi-file Maven project the whole
 * zip is passed (base64) in {@code additional_files}; {@code source_code} carries
 * the run script that the multi-file language executes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Judge0Request(
        @JsonProperty("source_code") String sourceCode,
        @JsonProperty("language_id") int languageId,
        @JsonProperty("stdin") String stdin,
        @JsonProperty("expected_output") String expectedOutput,
        @JsonProperty("cpu_time_limit") Integer cpuTimeLimit,
        @JsonProperty("memory_limit") Integer memoryLimit,
        @JsonProperty("additional_files") String additionalFiles) {
}
