import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.concurrent.atomic.*;

// __CANDIDATE_CODE__

public class Main {
    static void check(String n, boolean ok, String msg) {
        System.out.println(ok ? "TEST_PASS::" + n : "TEST_FAIL::" + n + "::" + msg);
    }
    public static void main(String[] args) {
        try {
            ResilientClient client = new ResilientClient();

            String ok = client.callWithFallback(() -> "LIVE", "FALLBACK", 3);
            check("returns upstream value on success", "LIVE".equals(ok), "got " + ok);

            String fb = client.callWithFallback(() -> { throw new RuntimeException("down"); }, "FALLBACK", 3);
            check("returns fallback when upstream always fails", "FALLBACK".equals(fb), "got " + fb);

            AtomicInteger n = new AtomicInteger(0);
            String retry = client.callWithFallback(() -> {
                if (n.incrementAndGet() < 3) throw new RuntimeException("flaky");
                return "RECOVERED";
            }, "FALLBACK", 3);
            check("retries until success within maxAttempts", "RECOVERED".equals(retry),
                    "got " + retry + " after " + n.get() + " attempts");

            AtomicInteger m = new AtomicInteger(0);
            String exhausted = client.callWithFallback(() -> {
                if (m.incrementAndGet() < 3) throw new RuntimeException("flaky");
                return "RECOVERED";
            }, "FALLBACK", 2);
            check("falls back when attempts are exhausted", "FALLBACK".equals(exhausted), "got " + exhausted);
        } catch (Throwable t) {
            System.out.println("TEST_FAIL::harness::unexpected " + t);
        }
    }
}
