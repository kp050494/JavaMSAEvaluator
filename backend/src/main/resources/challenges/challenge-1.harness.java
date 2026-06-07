import java.util.*;
import java.util.function.*;
import java.util.stream.*;

class Product {
    public long id; public String name; public double price; public String category;
    public Product(long id, String name, double price, String category) {
        this.id = id; this.name = name; this.price = price; this.category = category;
    }
}

// __CANDIDATE_CODE__

public class Main {
    static void check(String n, boolean ok, String msg) {
        System.out.println(ok ? "TEST_PASS::" + n : "TEST_FAIL::" + n + "::" + msg);
    }
    public static void main(String[] args) {
        try {
            ProductService s = new ProductService();
            Product p1 = s.add("Keyboard", 89.99, "Peripherals");
            Product p2 = s.add("Monitor", 199.5, "Displays");
            Product p3 = s.add("Hub", 34.0, "Accessories");
            check("add assigns id starting at 1", p1 != null && p1.id == 1,
                    "first id was " + (p1 == null ? "null" : p1.id));
            check("ids increment per product", p2 != null && p3 != null && p2.id == 2 && p3.id == 3,
                    "ids " + (p2 == null ? "null" : p2.id) + "," + (p3 == null ? "null" : p3.id));
            List<Product> all = s.getAll();
            check("getAll returns all products", all != null && all.size() == 3,
                    "size " + (all == null ? "null" : all.size()));
            Product found = s.findById(2);
            check("findById returns the matching product", found != null && "Monitor".equals(found.name),
                    "got " + (found == null ? "null" : found.name));
            check("findById returns null when missing", s.findById(999) == null, "expected null");
        } catch (Throwable t) {
            System.out.println("TEST_FAIL::harness::unexpected " + t);
        }
    }
}
