package com.assessment;

import com.assessment.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ===========================================================================
 *  CHALLENGE 1 - REST Controller Basics
 * ===========================================================================
 *  This file is REPLACED at runtime with the candidate's submission.
 *  The reference implementation below keeps the template compilable and
 *  serves as the "correct answer" the test-suite targets.
 *
 *  NOTE: top-level classes here are package-private on purpose so the file
 *  can be named CANDIDATE_SUBMISSION.java. When a candidate submits a
 *  `public class`, the backend writes it to a correctly-named file.
 * ===========================================================================
 */
@RestController
@RequestMapping("/api/products")
class ProductController {

    private final List<Product> products = new ArrayList<>();
    private final AtomicLong ids = new AtomicLong(0);

    ProductController() {
        save(new Product(null, "Mechanical Keyboard", 89.99, "Peripherals"));
        save(new Product(null, "27\" Monitor", 199.50, "Displays"));
        save(new Product(null, "USB-C Hub", 34.00, "Accessories"));
    }

    private Product save(Product p) {
        p.setId(ids.incrementAndGet());
        products.add(p);
        return p;
    }

    @GetMapping
    public List<Product> getProducts() {
        return products;
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(save(product));
    }
}
