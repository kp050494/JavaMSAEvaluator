-- Re-target the catalogue from Spring/Maven challenges to single-file plain-Java
-- exercises graded by the Piston API. The server-side test harness for each lives
-- in classpath:challenges/<slug>.harness.java (not stored in the DB).

UPDATE challenges SET
  difficulty = 'EASY', category = 'Collections', total_tests = 5,
  description = $d$Goal
Implement a small in-memory product catalogue as a plain Java class.

Write a class `ProductService` with:
  - Product add(String name, double price, String category)
      assigns an auto-incrementing id starting at 1, stores it, returns it.
  - List<Product> getAll()        -> all products added so far.
  - Product findById(long id)     -> the product with that id, or null if none.

Provided: a `Product` class with public fields (id, name, price, category) and a
constructor Product(id, name, price, category). java.util.* is already imported.

Acceptance criteria
  - add assigns id starting at 1
  - ids increment per product
  - getAll returns all products
  - findById returns the matching product
  - findById returns null when missing$d$,
  starter_code = $s$// Product is provided: new Product(id, name, price, category),
// with public fields id, name, price, category. java.util.* is already imported.

class ProductService {

    // TODO: keep a List<Product> and a counter for ids.

    public Product add(String name, double price, String category) {
        // TODO: create a Product with the next id (starting at 1), store it, return it
        return null;
    }

    public List<Product> getAll() {
        // TODO: return all products
        return new ArrayList<>();
    }

    public Product findById(long id) {
        // TODO: return the product with this id, or null if not found
        return null;
    }
}
$s$,
  concepts = $c$["Classes & objects","List / ArrayList","Auto-increment id","Linear search","null handling"]$c$,
  hints = $h$["Keep a List<Product> field and a long counter.","Pre-increment the counter (++seq) so the first id is 1.","Loop the list (or use streams) to match by id; return null if not found."]$h$,
  test_cases = $t$["add assigns id starting at 1","ids increment per product","getAll returns all products","findById returns the matching product","findById returns null when missing"]$t$
WHERE slug = 'challenge-1';

UPDATE challenges SET
  difficulty = 'EASY', category = 'Validation', total_tests = 4,
  description = $d$Goal
Validate product input and return a list of error messages.

Write a class `ProductValidator` with:
  - List<String> validate(String name, double price)
      Return an error mentioning "name" when name is null/blank,
      an error mentioning "price" when price <= 0,
      and an EMPTY list when the input is valid.

java.util.* is already imported.

Acceptance criteria
  - valid input produces no errors
  - blank name is rejected
  - non-positive price is rejected
  - two violations produce two errors$d$,
  starter_code = $s$// java.util.* is already imported.

class ProductValidator {

    // Return a list of error messages for the given fields:
    //   - blank/empty name  -> a message containing "name"
    //   - price <= 0        -> a message containing "price"
    // Return an EMPTY list when everything is valid.
    public List<String> validate(String name, double price) {
        // TODO
        return new ArrayList<>();
    }
}
$s$,
  concepts = $c$["Input validation","Conditionals","String trim / isEmpty","Building a List of messages"]$c$,
  hints = $h$["Start with an empty ArrayList and add messages as you find problems.","A blank name is null, empty, or only whitespace (use trim()).","Return the list at the end — empty means valid."]$h$,
  test_cases = $t$["valid input produces no errors","blank name is rejected","non-positive price is rejected","two violations produce two errors"]$t$
WHERE slug = 'challenge-2';

UPDATE challenges SET
  difficulty = 'MEDIUM', category = 'Data Filtering', total_tests = 5,
  description = $d$Goal
Implement an in-memory repository with simple queries.

Write a class `ProductRepository` with:
  - void save(Product p)
  - List<Product> findAll()
  - Product findById(long id)                      -> matching product or null
  - List<Product> findByCategory(String category)  -> products in that category
  - List<Product> findByPriceLessThan(double max)  -> products cheaper than max

Provided: a `Product` class (public fields id, name, price, category).
java.util.* is already imported.

Acceptance criteria
  - findAll returns all saved entities
  - findById returns the right entity
  - findByCategory filters by category
  - findByPriceLessThan filters by price
  - findByCategory returns empty for an unknown category$d$,
  starter_code = $s$// Product is provided (public fields id, name, price, category).
// java.util.* is already imported.

class ProductRepository {

    // TODO: back the repository with a List<Product>.

    public void save(Product p) {
        // TODO
    }

    public List<Product> findAll() {
        // TODO
        return new ArrayList<>();
    }

    public Product findById(long id) {
        // TODO: matching product or null
        return null;
    }

    public List<Product> findByCategory(String category) {
        // TODO: only products in this category
        return new ArrayList<>();
    }

    public List<Product> findByPriceLessThan(double max) {
        // TODO: only products with price < max
        return new ArrayList<>();
    }
}
$s$,
  concepts = $c$["Encapsulation","Filtering collections","Streams or loops","equals for Strings"]$c$,
  hints = $h$["Back it with a List<Product>.","Filter with a loop, or stream().filter(...).collect(Collectors.toList()).","Compare categories with equals(), not =="]$h$,
  test_cases = $t$["findAll returns all saved entities","findById returns the right entity","findByCategory filters by category","findByPriceLessThan filters by price","findByCategory returns empty for an unknown category"]$t$
WHERE slug = 'challenge-3';

UPDATE challenges SET
  difficulty = 'MEDIUM', category = 'Exceptions', total_tests = 4,
  description = $d$Goal
Use exceptions to enforce business rules.

Write a class `OrderService` with:
  - int reserve(int stock, int qty)
      * throw IllegalArgumentException if qty <= 0
      * throw IllegalStateException   if qty > stock
      * otherwise return the remaining stock (stock - qty)

Acceptance criteria
  - valid reservation returns remaining stock
  - exact-stock reservation returns 0
  - non-positive quantity throws IllegalArgumentException
  - over-stock throws IllegalStateException$d$,
  starter_code = $s$class OrderService {

    // Enforce the rules with exceptions; otherwise return stock - qty.
    public int reserve(int stock, int qty) {
        // TODO
        return 0;
    }
}
$s$,
  concepts = $c$["Exceptions","IllegalArgumentException vs IllegalStateException","Guard clauses","Return values"]$c$,
  hints = $h$["Check qty <= 0 first and throw new IllegalArgumentException(...).","Then check qty > stock and throw new IllegalStateException(...).","Otherwise return stock - qty."]$h$,
  test_cases = $t$["valid reservation returns remaining stock","exact-stock reservation returns 0","non-positive quantity throws IllegalArgumentException","over-stock throws IllegalStateException"]$t$
WHERE slug = 'challenge-4';

UPDATE challenges SET
  difficulty = 'HARD', category = 'Resilience', total_tests = 4,
  description = $d$Goal
Make a flaky call resilient with retry + fallback.

Write a class `ResilientClient` with:
  - String callWithFallback(java.util.function.Supplier<String> upstream,
                            String fallback, int maxAttempts)
      Call upstream up to maxAttempts times. Return its value on the first success.
      If every attempt throws, return the fallback value.

java.util.* and java.util.function.* are already imported.

Acceptance criteria
  - returns upstream value on success
  - returns fallback when upstream always fails
  - retries until success within maxAttempts
  - falls back when attempts are exhausted$d$,
  starter_code = $s$// java.util.function.Supplier is available.

class ResilientClient {

    public String callWithFallback(Supplier<String> upstream, String fallback, int maxAttempts) {
        // TODO: try upstream.get() up to maxAttempts times; return the first success,
        //       otherwise return fallback.
        return fallback;
    }
}
$s$,
  concepts = $c$["Functional interfaces (Supplier)","try/catch","Retry loops","Graceful degradation"]$c$,
  hints = $h$["Loop from 0 to maxAttempts.","Inside the loop: try { return upstream.get(); } catch (Exception e) { /* retry */ }.","After the loop (all attempts failed), return the fallback."]$h$,
  test_cases = $t$["returns upstream value on success","returns fallback when upstream always fails","retries until success within maxAttempts","falls back when attempts are exhausted"]$t$
WHERE slug = 'challenge-5';

UPDATE challenges SET
  difficulty = 'MEDIUM', category = 'Security', total_tests = 5,
  description = $d$Goal
Implement a tiny stateless token scheme.

Write a class `TokenService` with:
  - String issue(String user, long expiresAt) -> a token encoding the user + expiry
  - boolean isValid(String token, long now)   -> true only if now < expiry
  - String subject(String token)              -> the user encoded in the token

A simple valid encoding is "user:expiry" (e.g. "ada:2000"). isValid must return
false (not throw) for a malformed token. java.util.* is already imported.

Acceptance criteria
  - issued token carries the subject
  - valid token is accepted before expiry
  - expired token is rejected
  - subject is extracted from the token
  - malformed token is rejected without throwing$d$,
  starter_code = $s$// java.util.* is already imported.

class TokenService {

    public String issue(String user, long expiresAt) {
        // TODO: encode the user and expiry into one token string
        return null;
    }

    public boolean isValid(String token, long now) {
        // TODO: true only if the token is well-formed and now < expiry
        return false;
    }

    public String subject(String token) {
        // TODO: return the user encoded in the token
        return null;
    }
}
$s$,
  concepts = $c$["String encode/parse","Long.parseLong","Defensive parsing","Stateless tokens"]$c$,
  hints = $h$["issue can simply return user + \":\" + expiresAt.","Split on the last ':' and guard against -1 (no colon present).","Wrap Long.parseLong in try/catch and return false on failure."]$h$,
  test_cases = $t$["issued token carries the subject","valid token is accepted before expiry","expired token is rejected","subject is extracted from the token","malformed token is rejected without throwing"]$t$
WHERE slug = 'challenge-6';
