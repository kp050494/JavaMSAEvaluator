package com.assessment.service;

import com.assessment.model.TestCaseResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Shared logic for the remote single-file Java executors (Paiza / Piston):
 * assemble the runnable file from a challenge harness + candidate code, and parse
 * the TEST_PASS:: / TEST_FAIL:: markers the harness prints to stdout.
 */
@Component
public class HarnessSupport {

    public static final String PLACEHOLDER = "// __CANDIDATE_CODE__";
    private static final String PASS = "TEST_PASS::";
    private static final String FAIL = "TEST_FAIL::";

    private static final Pattern PACKAGE = Pattern.compile("(?m)^\\s*package\\b.*;\\s*$");
    private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*import\\b.*;\\s*$");
    private static final Pattern PUBLIC_TYPE =
            Pattern.compile("(?m)^(\\s*)public\\s+((?:final\\s+|abstract\\s+)?(?:class|interface|enum|record)\\b)");

    private final Map<String, String> harnessCache = new ConcurrentHashMap<>();

    /** Build the single Main.java to execute: harness with the candidate code injected. */
    public String assemble(String slug, String candidateCode) {
        return loadHarness(slug).replace(PLACEHOLDER, sanitize(candidateCode));
    }

    private String loadHarness(String slug) {
        return harnessCache.computeIfAbsent(slug, s -> {
            try {
                var resource = new ClassPathResource("challenges/" + s + ".harness.java");
                return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new IllegalStateException("Missing harness for challenge " + s, e);
            }
        });
    }

    /**
     * Make candidate code safe to inline into the single harness file: drop
     * package/import lines (the harness imports what's needed) and demote any
     * top-level {@code public} type so it doesn't clash with the public Main class.
     */
    static String sanitize(String code) {
        if (code == null) {
            return "";
        }
        String out = PACKAGE.matcher(code).replaceAll("");
        out = IMPORT.matcher(out).replaceAll("");
        out = PUBLIC_TYPE.matcher(out).replaceAll("$1$2");
        return out.strip();
    }

    public List<TestCaseResult> parseMarkers(String stdout) {
        List<TestCaseResult> results = new ArrayList<>();
        if (stdout == null) {
            return results;
        }
        for (String raw : stdout.split("\\R")) {
            String line = raw.trim();
            if (line.startsWith(PASS)) {
                results.add(new TestCaseResult(line.substring(PASS.length()).trim(), true, null));
            } else if (line.startsWith(FAIL)) {
                String[] parts = line.substring(FAIL.length()).split("::", 2);
                results.add(new TestCaseResult(parts[0].trim(), false, parts.length > 1 ? parts[1].trim() : "failed"));
            }
        }
        return results;
    }
}
