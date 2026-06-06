package com.assessment.support;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Optional;

/**
 * Emits machine-parseable per-test markers to stdout for the backend to parse.
 *   JUNIT_RESULT::<testName>::PASSED
 *   JUNIT_RESULT::<testName>::FAILED::<single-line message>
 */
public class ResultPrinter implements TestWatcher {

    public static final String PREFIX = "JUNIT_RESULT::";

    private static String name(ExtensionContext ctx) {
        return ctx.getDisplayName().replaceAll("[\\r\\n]+", " ").replace("()", "");
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        System.out.println(PREFIX + name(context) + "::PASSED");
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String msg = cause == null ? "assertion failed" : String.valueOf(cause.getMessage());
        msg = msg == null ? cause.getClass().getSimpleName() : msg.replaceAll("[\\r\\n]+", " ");
        if (msg.length() > 300) {
            msg = msg.substring(0, 300) + "...";
        }
        System.out.println(PREFIX + name(context) + "::FAILED::" + msg);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        System.out.println(PREFIX + name(context) + "::FAILED::aborted");
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        System.out.println(PREFIX + name(context) + "::FAILED::disabled");
    }
}
