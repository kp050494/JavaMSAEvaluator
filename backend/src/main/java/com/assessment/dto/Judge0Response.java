package com.assessment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Judge0Response(
        String token,
        String stdout,
        String stderr,
        @JsonProperty("compile_output") String compileOutput,
        String message,
        String time,
        Integer memory,
        Status status) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Status(int id, String description) {
    }

    /** Judge0 status ids: 1=In Queue, 2=Processing, >=3 means finished. */
    public boolean isFinished() {
        return status != null && status.id() >= 3;
    }
}
