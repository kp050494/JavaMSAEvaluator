package com.assessment;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * ===========================================================================
 *  CHALLENGE 5 - Resilience: Circuit Breaker, Fallback & Timeout
 * ===========================================================================
 *  Reference implementation (replaced at runtime with candidate code).
 *  Call an upstream product-service over HTTP and make the call resilient:
 *    - apply a read/connect timeout (upstream.timeout-ms),
 *    - wrap the call with a Resilience4j @CircuitBreaker,
 *    - return a fallback payload when the upstream errors or times out.
 *  Expose the result at GET /api/external/products.
 * ===========================================================================
 */
@Configuration
class ClientConfig {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder,
                              @Value("${upstream.timeout-ms:1000}") long timeoutMs) {
        return builder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }
}

@Service
class ProductClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    ProductClient(RestTemplate restTemplate, @Value("${upstream.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallback")
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getProducts() {
        return restTemplate.getForObject(baseUrl + "/products", List.class);
    }

    @SuppressWarnings("unused")
    List<Map<String, Object>> fallback(Throwable t) {
        return List.of(Map.of(
                "name", "FALLBACK",
                "source", "fallback",
                "reason", t.getClass().getSimpleName()));
    }
}

@RestController
class ExternalProductController {

    private final ProductClient client;

    ExternalProductController(ProductClient client) {
        this.client = client;
    }

    @GetMapping("/api/external/products")
    public List<Map<String, Object>> products() {
        return client.getProducts();
    }
}
