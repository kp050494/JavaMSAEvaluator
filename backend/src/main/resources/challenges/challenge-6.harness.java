import java.util.*;
import java.util.function.*;
import java.util.stream.*;

// __CANDIDATE_CODE__

public class Main {
    static void check(String n, boolean ok, String msg) {
        System.out.println(ok ? "TEST_PASS::" + n : "TEST_FAIL::" + n + "::" + msg);
    }
    public static void main(String[] args) {
        try {
            TokenService ts = new TokenService();
            long now = 1000L;
            String token = ts.issue("ada", 2000L);
            check("issued token carries the subject", token != null && token.contains("ada"), "token=" + token);
            check("valid token is accepted before expiry", ts.isValid(token, now), "expected valid when now < expiry");
            check("expired token is rejected", !ts.isValid(token, 3000L), "expected invalid when now > expiry");
            check("subject is extracted from the token", "ada".equals(ts.subject(token)), "got " + ts.subject(token));
            check("malformed token is rejected without throwing", !ts.isValid("garbage", now), "expected false");
        } catch (Throwable t) {
            System.out.println("TEST_FAIL::harness::unexpected " + t);
        }
    }
}
