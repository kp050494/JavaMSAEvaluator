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
            ProductValidator v = new ProductValidator();
            List<String> ok = v.validate("Keyboard", 10.0);
            check("valid input produces no errors", ok != null && ok.isEmpty(), "errors=" + ok);
            List<String> blank = v.validate("   ", 10.0);
            check("blank name is rejected", blank != null && blank.stream().anyMatch(e -> e.toLowerCase().contains("name")), "errors=" + blank);
            List<String> price = v.validate("Keyboard", 0.0);
            check("non-positive price is rejected", price != null && price.stream().anyMatch(e -> e.toLowerCase().contains("price")), "errors=" + price);
            List<String> both = v.validate("", -5.0);
            check("two violations produce two errors", both != null && both.size() == 2, "errors=" + both);
        } catch (Throwable t) {
            System.out.println("TEST_FAIL::harness::unexpected " + t);
        }
    }
}
