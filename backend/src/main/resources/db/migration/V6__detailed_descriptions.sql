-- Expand each challenge's problem statement into a detailed, unambiguous spec:
-- goal, what's provided, exact requirements, and acceptance criteria.

UPDATE challenges SET description = $C1$Goal
Implement an in-memory REST controller for a Product catalogue.

Provided for you (do not redefine)
  - Product (model): fields id, name, price, category, with getters/setters and a
    Product(id, name, price, category) constructor.
  - Application: the Spring Boot entry point.

Implement a @RestController mapped at /api/products that:
  1. Seeds a few Products in its constructor (so the list is never empty).
  2. GET  /api/products       -> 200 OK with a JSON array of all products.
  3. GET  /api/products/{id}  -> 200 OK with the matching product, or
                                 404 Not Found if none (throw ResponseStatusException).
  4. POST /api/products       -> 201 Created, returns the saved product with a
                                 generated id.

Acceptance criteria (what the tests check)
  - GET /api/products returns a non-empty array.
  - Each product exposes id, name, price and category.
  - GET /api/products/1 returns the first seeded product.
  - GET /api/products/99999 returns 404.
  - POST /api/products returns 201 with a generated id.$C1$
WHERE slug = 'challenge-1';

UPDATE challenges SET description = $C2$Goal
Move storage into a @Service and enforce request validation.

Provided for you (do not redefine)
  - Product (model) with constraints already declared: @NotBlank name, @Positive price.
  - Application.

Implement
  1. A @Service (e.g. ProductService) that stores products in memory and exposes
     create(Product) (assigns an id) and findAll().
  2. A @RestController at /api/products that constructor-injects the service:
       - GET  /api/products -> 200 OK with all products.
       - POST /api/products -> validate the body with @Valid:
            * blank name -> 400 Bad Request
            * price <= 0 -> 400 Bad Request
            * otherwise  -> 201 Created with the saved product.

Acceptance criteria (what the tests check)
  - A ProductService bean exists in the application context.
  - A valid POST returns 201 with a generated id.
  - A blank name returns 400.
  - A non-positive price returns 400.
  - A created product then appears in GET /api/products.$C2$
WHERE slug = 'challenge-2';

UPDATE challenges SET description = $C3$Goal
Persist products with Spring Data JPA.

Provided for you (do not redefine)
  - Product: a JPA @Entity (table "products") with @Id @GeneratedValue.
  - Application. The tests run against an embedded H2 database.

Implement a Spring Data repository interface for Product that provides:
  - Standard CRUD by extending JpaRepository<Product, Long>
    (save / findById / findAll / deleteAll come for free).
  - A derived query:  List<Product> findByCategory(String category)
  - A derived query:  List<Product> findByPriceLessThan(Double price)

Acceptance criteria (what the tests check)
  - save() then findById() round-trips an entity.
  - findByCategory returns only products in that category.
  - findByPriceLessThan returns only products cheaper than the threshold.
  - The schema is created and seeded rows are returned by findAll().$C3$
WHERE slug = 'challenge-3';

UPDATE challenges SET description = $C4$Goal
Return one consistent JSON error contract for the whole API.

Provided for you (do not redefine)
  - A controller that deliberately raises errors:
        GET  /api/products/{id} -> throws ResourceNotFoundException
        POST /api/products      -> @Valid body (invalid -> MethodArgumentNotValidException)
        GET  /api/boom          -> throws a RuntimeException
  - ResourceNotFoundException, Product (@NotBlank/@Positive), Application.

Implement a @RestControllerAdvice that converts failures into an ErrorResponse JSON
body with exactly these fields: timestamp, status, message, path
(plus an errors array for validation failures). Map:
  - ResourceNotFoundException        -> 404
  - MethodArgumentNotValidException  -> 400, errors[] of "field: message"
  - any other Exception              -> 500
Use HttpServletRequest.getRequestURI() for path. You may define ErrorResponse yourself.

Acceptance criteria (what the tests check)
  - 404 response has timestamp, status=404, message and path=/api/products/99999.
  - An invalid POST returns 400 with a non-empty errors array and path=/api/products.
  - GET /api/boom returns 500 with the same consistent shape.$C4$
WHERE slug = 'challenge-4';

UPDATE challenges SET description = $C5$Goal
Call a remote product-service over HTTP and make the call resilient.

Provided for you (do not redefine)
  - application.yml properties: upstream.base-url and upstream.timeout-ms.
  - Resilience4j on the classpath with a pre-configured circuit breaker instance
    named "productService". Application is provided.
  - In the tests a WireMock server stands in for the upstream service.

Implement
  1. A RestTemplate bean whose connect AND read timeouts are set from upstream.timeout-ms.
  2. A client (@Service) with a method annotated
     @CircuitBreaker(name = "productService", fallbackMethod = "fallback")
     that GETs {upstream.base-url}/products and returns the body.
  3. A fallback(Throwable t) method (same return type) returning a default payload
     that contains source = "fallback".
  4. A @RestController exposing GET /api/external/products -> the client result.

Acceptance criteria (what the tests check)
  - When the upstream returns data, it is passed through (source = "upstream").
  - When the upstream returns 503, the fallback payload is returned (source = "fallback").
  - When the upstream is slow, the read timeout trips and the fallback is returned.$C5$
WHERE slug = 'challenge-5';

UPDATE challenges SET description = $C6$Goal
Secure the API with stateless JWT authentication.

Provided for you (do not redefine)
  - A protected GET /api/products controller.
  - application.yml: security.jwt.secret (HS256 signing secret) and security.jwt.expiry-ms.
  - jjwt and Spring Security on the classpath. Application is provided.

Implement
  1. A JwtService that signs a token for a username (Keys.hmacShaKeyFor(secret bytes),
     HS256) and validates/parses a token (rejecting invalid or expired ones).
  2. A OncePerRequestFilter that reads "Authorization: Bearer <token>" and, on a valid
     token, sets the SecurityContext authentication.
  3. A SecurityFilterChain: stateless sessions, CSRF disabled, /auth/** public,
     every other request authenticated, and a 401 entry point for unauthenticated access.
  4. POST /auth/login accepting {"username","password"}:
       - valid credentials (username "user", password "password") -> 200 + {"token":"<jwt>"}
       - otherwise -> 401.

Acceptance criteria (what the tests check)
  - Login with valid credentials returns 200 with a token.
  - GET /api/products without a token returns 401.
  - GET /api/products with a valid Bearer token returns 200.
  - GET /api/products with an expired token returns 401.$C6$
WHERE slug = 'challenge-6';
