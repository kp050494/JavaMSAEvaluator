-- Challenges catalogue. List columns (concepts/hints/test_cases) are stored as
-- JSON text and converted to List<String> by a JPA AttributeConverter.
CREATE TABLE challenges (
    id                BIGSERIAL PRIMARY KEY,
    slug              VARCHAR(64) NOT NULL UNIQUE,
    order_index       INT NOT NULL,
    title             VARCHAR(160) NOT NULL,
    difficulty        VARCHAR(16) NOT NULL,
    category          VARCHAR(64) NOT NULL,
    description       TEXT NOT NULL,
    starter_code      TEXT NOT NULL,
    concepts          TEXT NOT NULL,
    hints             TEXT NOT NULL,
    test_cases        TEXT NOT NULL,
    template_dir      VARCHAR(64) NOT NULL,
    total_tests       INT NOT NULL,
    estimated_minutes INT NOT NULL
);

INSERT INTO challenges
(slug, order_index, title, difficulty, category, description, starter_code, concepts, hints, test_cases, template_dir, total_tests, estimated_minutes)
VALUES
(
 'challenge-1', 1, 'REST Controller Basics', 'EASY', 'REST API',
 $desc$Build a Spring MVC REST controller for a Product catalogue.

Expose:
  - GET  /api/products        -> 200 with a JSON array of products
  - GET  /api/products/{id}   -> 200 with the product, or 404 if missing
  - POST /api/products        -> 201 Created with the saved product

Each product has: id, name, price, category. Seed a few products in the
controller's constructor so the list endpoint returns data.$desc$,
 $code$package com.assessment;

import com.assessment.model.Product;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/products")
class ProductController {

    // TODO: keep an in-memory list of products and seed a few of them.

    @GetMapping
    public List<Product> getProducts() {
        // TODO: return all products
        return List.of();
    }

    // TODO: GET /{id} -> return product or throw 404
    // TODO: POST     -> save and return 201 Created
}
$code$,
 $concepts$["@RestController","@RequestMapping","@GetMapping / @PostMapping","@PathVariable","ResponseEntity","ResponseStatusException"]$concepts$,
 $hints$["A @RestController serialises returned objects to JSON automatically.","Throw new ResponseStatusException(HttpStatus.NOT_FOUND) for missing ids.","Return ResponseEntity.status(HttpStatus.CREATED).body(saved) for POST."]$hints$,
 $tc$["GET /api/products returns 200 and a JSON array","GET /api/products/99999 returns 404","Products expose id, name, price and category fields"]$tc$,
 'challenge-1', 3, 12
),
(
 'challenge-2', 2, 'Service Layer & Bean Validation', 'EASY', 'Validation',
 $desc$Refactor the controller to delegate to a @Service and enforce bean
validation on incoming products.

Requirements:
  - Introduce a @Service that stores products and is injected into the controller.
  - Annotate the POST body with @Valid so the model constraints are enforced:
      * blank name        -> 400 Bad Request
      * price <= 0        -> 400 Bad Request
  - A valid product POST returns 201 Created with a generated id.$desc$,
 $code$package com.assessment;

import com.assessment.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Service
class ProductService {
    // TODO: store products and expose create(...) / findAll()
}

@RestController
@RequestMapping("/api/products")
class ProductController {

    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    // TODO: GET  -> productService.findAll()
    // TODO: POST -> @Valid body, return 201 Created
}
$code$,
 $concepts$["@Service","Constructor injection","@Valid","Jakarta Bean Validation","@ResponseStatus / 201 Created"]$concepts$,
 $hints$["The Product model already declares @NotBlank and @Positive.","Add @Valid before @RequestBody to trigger validation.","An invalid body raises MethodArgumentNotValidException -> 400 by default."]$hints$,
 $tc$["A ProductService @Service bean exists in the context","Valid product POST returns 201 with an id","Blank name returns 400","Non-positive price returns 400","Saved products appear in the list endpoint"]$tc$,
 'challenge-2', 5, 15
),
(
 'challenge-3', 3, 'Spring Data JPA Repository', 'MEDIUM', 'Persistence',
 $desc$Persist products with Spring Data JPA.

Write a repository interface for the Product @Entity that provides the standard
CRUD operations plus two derived queries:
  - findByCategory(String category)
  - findByPriceLessThan(Double price)

The tests run against an embedded H2 database created from the entity mapping.$desc$,
 $code$package com.assessment;

import com.assessment.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// TODO: extend JpaRepository<Product, Long> and declare the derived queries.
interface ProductRepository extends JpaRepository<Product, Long> {

    // List<Product> findByCategory(String category);
    // List<Product> findByPriceLessThan(Double price);
}
$code$,
 $concepts$["Spring Data JPA","JpaRepository","Derived query methods","@Entity / @Id / @GeneratedValue","Embedded H2"]$concepts$,
 $hints$["JpaRepository<Product, Long> gives save/findById/findAll for free.","Method names map to queries: findByCategory, findByPriceLessThan.","Return List<Product> from the derived query methods."]$hints$,
 $tc$["Repository saves and finds an entity by id","findByCategory returns only matching products","findByPriceLessThan filters by price","Schema is created and seeded rows are returned"]$tc$,
 'challenge-3', 4, 18
),
(
 'challenge-4', 4, 'Global Exception Handling', 'MEDIUM', 'Error Handling',
 $desc$Provide a single, consistent error contract for the whole API.

Write a @RestControllerAdvice that converts failures into an ErrorResponse JSON
body containing: timestamp, status, message, path (plus errors[] for validation).

Map:
  - ResourceNotFoundException        -> 404
  - MethodArgumentNotValidException  -> 400 with per-field error messages
  - any other Exception              -> 500

The provided controller raises these errors for you to handle.$desc$,
 $code$package com.assessment;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
class GlobalExceptionHandler {

    // TODO: @ExceptionHandler(ResourceNotFoundException.class) -> 404
    // TODO: @ExceptionHandler(MethodArgumentNotValidException.class) -> 400 + field errors
    // TODO: @ExceptionHandler(Exception.class) -> 500
    // Build an ErrorResponse with timestamp, status, message, path.
}
$code$,
 $concepts$["@RestControllerAdvice","@ExceptionHandler","ResponseEntity","MethodArgumentNotValidException","Consistent error contract"]$concepts$,
 $hints$["Use HttpServletRequest.getRequestURI() to fill the path field.","ex.getBindingResult().getFieldErrors() gives field-level messages.","A handler for Exception.class catches everything else as 500."]$hints$,
 $tc$["404 returns the ErrorResponse shape (timestamp/status/message/path)","Invalid body returns 400 with field errors","500 returns the same consistent shape"]$tc$,
 'challenge-4', 3, 17
),
(
 'challenge-5', 5, 'Resilience: Circuit Breaker & Fallback', 'HARD', 'Resilience',
 $desc$Call a remote product-service over HTTP and make the call resilient.

Requirements:
  - Apply a connect/read timeout from upstream.timeout-ms.
  - Wrap the call with a Resilience4j @CircuitBreaker(name = "productService").
  - Provide a fallback method that returns a default payload (source = "fallback")
    when the upstream errors or times out.
  - Expose the result at GET /api/external/products.

The tests use WireMock to stand in for the upstream service.$desc$,
 $code$package com.assessment;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@Configuration
class ClientConfig {
    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder,
                              @Value("${upstream.timeout-ms:1000}") long timeoutMs) {
        // TODO: apply connect + read timeouts
        return builder.build();
    }
}

@Service
class ProductClient {
    // TODO: @CircuitBreaker(name = "productService", fallbackMethod = "fallback")
    //       call {upstream.base-url}/products and return the body.
    // TODO: fallback(Throwable t) returning a default payload.
}

@RestController
class ExternalProductController {
    // TODO: GET /api/external/products -> productClient.getProducts()
}
$code$,
 $concepts$["Resilience4j","@CircuitBreaker","Fallback methods","RestTemplate timeouts","Graceful degradation"]$concepts$,
 $hints$["A Resilience4j fallback fires on ANY exception thrown by the method.","Set connect and read timeouts on the RestTemplate via RestTemplateBuilder.","The fallback signature must match the method return type plus a Throwable param."]$hints$,
 $tc$["Upstream success passes data through","Upstream 503 triggers the fallback payload","A slow upstream times out to the fallback (timeout respected)"]$tc$,
 'challenge-5', 3, 20
),
(
 'challenge-6', 6, 'Stateless JWT Security', 'MEDIUM', 'Security',
 $desc$Secure the API with stateless JWT authentication.

Requirements:
  - POST /auth/login with {"username":"user","password":"password"} returns 200
    and a body {"token": "<jwt>"}.
  - /auth/** is public; every other endpoint requires a valid Bearer token.
  - A missing, invalid or expired token returns 401.

Tokens are signed with the secret in application.yml (security.jwt.secret).$desc$,
 $code$package com.assessment;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

// TODO: JwtService  - generate(username) + validateAndGetSubject(token) with jjwt
// TODO: JwtAuthFilter (OncePerRequestFilter) - authenticate the Bearer token
// TODO: AuthController POST /auth/login -> issue token for valid creds
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // TODO: stateless, permit /auth/**, authenticate the rest,
        //       401 entry point, add the JWT filter.
        return http.build();
    }
}
$code$,
 $concepts$["Spring Security 6","SecurityFilterChain","OncePerRequestFilter","JWT (jjwt)","Stateless sessions","AuthenticationEntryPoint"]$concepts$,
 $hints$["Sign with Keys.hmacShaKeyFor(secret.getBytes()) and the configured secret.","Set SessionCreationPolicy.STATELESS and disable CSRF for a token API.","On an invalid token, clear the context so the 401 entry point fires."]$hints$,
 $tc$["Login with valid credentials returns 200 and a token","Protected endpoint without a token returns 401","Protected endpoint with a valid token returns 200","Protected endpoint with an expired token returns 401","Login with bad credentials is rejected"]$tc$,
 'challenge-6', 5, 15
);
