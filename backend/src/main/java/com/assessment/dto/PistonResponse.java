package com.assessment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response from the Piston execute API (https://github.com/engineer-man/piston).
 * For compiled languages both a {@code compile} and a {@code run} stage are returned.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PistonResponse(String language, String version, Stage run, Stage compile) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Stage(String stdout, String stderr, String output, Integer code, String signal) {
    }

    public boolean compileFailed() {
        return compile != null && compile.code() != null && compile.code() != 0;
    }
}
