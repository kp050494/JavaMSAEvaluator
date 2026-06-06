package com.assessment;

import com.assessment.model.Product;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ===========================================================================
 *  CHALLENGE 2 - Service Layer + Bean Validation
 * ===========================================================================
 *  Reference implementation (replaced at runtime with candidate code).
 *  Wire a @Service into the controller and enforce @Valid so that:
 *    - blank name           -> 400
 *    - price <= 0           -> 400
 *    - valid product POST   -> 201 Created
 * ===========================================================================
 */
@Service
class ProductService {

    private final List<Product> store = new ArrayList<>();
    private final AtomicLong ids = new AtomicLong(0);

    Product create(Product product) {
        product.setId(ids.incrementAndGet());
        store.add(product);
        return product;
    }

    List<Product> findAll() {
        return store;
    }
}

@RestController
@RequestMapping("/api/products")
class ProductController {

    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> all() {
        return productService.findAll();
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody Product product) {
        Product saved = productService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
