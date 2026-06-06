package com.assessment;

import com.assessment.model.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Fixed protected resource. The candidate must secure /api/** with JWT so that
 * this endpoint is only reachable with a valid Bearer token. Do not modify.
 */
@RestController
@RequestMapping("/api/products")
class ProductController {

    @GetMapping
    List<Product> all() {
        return List.of(
                new Product(1L, "Mechanical Keyboard", 89.99, "Peripherals"),
                new Product(2L, "27\" Monitor", 199.50, "Displays"));
    }
}
