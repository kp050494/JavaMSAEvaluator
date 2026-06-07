package com.assessment.service;

import com.assessment.model.Challenge;
import com.assessment.model.TestCaseResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Heuristic grader for free hosting where a privileged Judge0 sandbox isn't
 * available. It does NOT compile or run code — it pattern-matches the submission
 * against the key constructs each challenge requires, so the UI still streams a
 * believable, code-responsive pass/fail breakdown. Clearly labelled as simulated.
 *
 * Active when execution.mode=demo.
 */
@Service
@ConditionalOnProperty(name = "execution.mode", havingValue = "demo")
public class DemoExecutor implements SubmissionExecutor {

    // Per challenge (by slug): one entry per test, each entry is a set of regexes
    // that must ALL be present in the candidate code for that test to "pass".
    // Order matches the challenge's test_cases descriptions.
    private static final Map<String, String[][]> CHECKS = Map.of(
            "challenge-1", new String[][]{
                    {"new\\s+Product"},
                    {"\\+\\+|\\+\\s*1|incrementAndGet"},
                    {"\\breturn\\b"},
                    {"findById|for\\s*\\(|stream\\("},
                    {"return\\s+null"}
            },
            "challenge-2", new String[][]{
                    {"isEmpty|new\\s+ArrayList|List\\.of"},
                    {"name|trim|isBlank|isEmpty"},
                    {"price\\b"},
                    {"add\\(|\\.add\\("}
            },
            "challenge-3", new String[][]{
                    {"\\.add\\(|save"},
                    {"findById|for\\s*\\(|stream\\("},
                    {"findByCategory|getCategory|\\.category|equals\\("},
                    {"findByPriceLessThan|\\.price|<\\s*max"},
                    {"isEmpty|new\\s+ArrayList"}
            },
            "challenge-4", new String[][]{
                    {"return\\s+stock\\s*-\\s*qty|stock\\s*-\\s*qty"},
                    {"stock\\s*-\\s*qty|return\\s+0"},
                    {"IllegalArgumentException"},
                    {"IllegalStateException"}
            },
            "challenge-5", new String[][]{
                    {"upstream\\.get\\(\\)|\\.get\\(\\)"},
                    {"return\\s+fallback|fallback"},
                    {"for\\s*\\(|while\\s*\\(|maxAttempts"},
                    {"catch\\s*\\("}
            },
            "challenge-6", new String[][]{
                    {"user|\\+\\s*\":\"|return"},
                    {"now\\s*<|expiry|exp|parseLong"},
                    {"now\\s*<|expiry|exp|parseLong"},
                    {"substring|split|indexOf|lastIndexOf"},
                    {"try|catch|indexOf|return\\s+false"}
            }
    );

    @Override
    public String runningMessage() {
        return "Grading your code (DEMO MODE — sandbox execution simulated)...";
    }

    @Override
    public ExecutionResult run(Challenge challenge, String code) {
        String src = code == null ? "" : code;
        String[][] checks = CHECKS.get(challenge.getSlug());
        List<String> names = challenge.getTestCases();
        List<TestCaseResult> results = new ArrayList<>();

        if (checks == null) {
            // Unknown challenge: pass only if something substantial was submitted.
            boolean ok = src.strip().length() > 40;
            results.add(new TestCaseResult("submission received", ok,
                    ok ? null : "no meaningful code submitted"));
        } else {
            for (int i = 0; i < checks.length; i++) {
                String name = (names != null && names.size() > i) ? names.get(i) : "Check " + (i + 1);
                boolean passed = matchesAll(src, checks[i]);
                results.add(new TestCaseResult(name, passed,
                        passed ? null : "expected construct not found in your code (demo heuristic)"));
            }
        }

        StringBuilder log = new StringBuilder();
        log.append("================ DEMO MODE ================\n");
        log.append("Real sandbox execution is disabled on this free deployment, so your\n");
        log.append("submission is graded heuristically (pattern checks against your code),\n");
        log.append("not compiled or run. Run with execution.mode=judge0 for real grading.\n\n");
        for (TestCaseResult r : results) {
            log.append(r.passed() ? "PASS  " : "FAIL  ").append(r.testName()).append('\n');
        }
        return new ExecutionResult(results, log.toString());
    }

    private static boolean matchesAll(String code, String[] regexes) {
        for (String regex : regexes) {
            if (!Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(code).find()) {
                return false;
            }
        }
        return true;
    }
}
