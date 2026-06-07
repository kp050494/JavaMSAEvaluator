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
            ProductRepository repo = new ProductRepository();
            repo.save(new Product(1, "Keyboard", 89.99, "Peripherals"));
            repo.save(new Product(2, "Hub", 34.0, "Peripherals"));
            repo.save(new Product(3, "Monitor", 199.5, "Displays"));

            check("findAll returns all saved entities", repo.findAll() != null && repo.findAll().size() == 3,
                    "size=" + (repo.findAll() == null ? "null" : repo.findAll().size()));
            Product byId = repo.findById(3);
            check("findById returns the right entity", byId != null && "Monitor".equals(byId.name),
                    "got " + (byId == null ? "null" : byId.name));
            List<Product> per = repo.findByCategory("Peripherals");
            check("findByCategory filters by category",
                    per != null && per.size() == 2 && per.stream().allMatch(p -> "Peripherals".equals(p.category)),
                    "got " + (per == null ? "null" : per.size()));
            List<Product> cheap = repo.findByPriceLessThan(100.0);
            check("findByPriceLessThan filters by price",
                    cheap != null && cheap.size() == 2 && cheap.stream().allMatch(p -> p.price < 100.0),
                    "got " + (cheap == null ? "null" : cheap.size()));
            check("findByCategory returns empty for an unknown category",
                    repo.findByCategory("Nope") != null && repo.findByCategory("Nope").isEmpty(), "expected empty");
        } catch (Throwable t) {
            System.out.println("TEST_FAIL::harness::unexpected " + t);
        }
    }
}
