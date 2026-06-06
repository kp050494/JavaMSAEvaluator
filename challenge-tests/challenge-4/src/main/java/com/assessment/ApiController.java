package com.assessment;

import com.assessment.model.Product;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fixed controller that deliberately raises errors so the candidate's global
 * exception handler can be exercised. Do not modify.
 *
 *   GET  /api/products/{id} -> throws ResourceNotFoundException (expect 404)
 *   POST /api/products      -> @Valid body (invalid -> expect 400 field errors)
 *   GET  /api/boom          -> throws RuntimeException (expect 500)
 */
@RestController
@RequestMapping("/api")
class ApiController {

    @GetMapping("/products/{id}")
    Product getProduct(@PathVariable Long id) {
        throw new ResourceNotFoundException("Product " + id + " not found");
    }

    @PostMapping("/products")
    Product create(@Valid @RequestBody Product product) {
        return product;
    }

    @GetMapping("/boom")
    String boom() {
        throw new IllegalStateException("unexpected server failure");
    }
}
