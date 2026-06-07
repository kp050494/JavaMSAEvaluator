import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// __CANDIDATE_CODE__

public class Main {
    static void check(String n, boolean ok, String msg) {
        System.out.println(ok ? "TEST_PASS::" + n : "TEST_FAIL::" + n + "::" + msg);
    }
    static boolean throwsType(Runnable r, Class<?> type) {
        try { r.run(); return false; } catch (Throwable e) { return type.isInstance(e); }
    }
    public static void main(String[] args) {
        OrderService svc = new OrderService();
        try {
            int remaining = svc.reserve(10, 3);
            check("valid reservation returns remaining stock", remaining == 7, "got " + remaining);
        } catch (Throwable t) {
            check("valid reservation returns remaining stock", false, "threw " + t);
        }
        try {
            check("exact-stock reservation returns 0", svc.reserve(5, 5) == 0, "expected 0");
        } catch (Throwable t) {
            check("exact-stock reservation returns 0", false, "threw " + t);
        }
        check("non-positive quantity throws IllegalArgumentException",
                throwsType(() -> svc.reserve(10, 0), IllegalArgumentException.class),
                "expected IllegalArgumentException");
        check("over-stock throws IllegalStateException",
                throwsType(() -> svc.reserve(3, 5), IllegalStateException.class),
                "expected IllegalStateException");
    }
}
