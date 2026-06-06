-- Challenge 1's suite was tightened so the unimplemented starter no longer
-- scores partial credit (empty array / default 404). Reflect the new 5-test
-- suite in the catalogue metadata used for display.
UPDATE challenges
SET total_tests = 5,
    test_cases = '["GET /api/products returns a non-empty seeded array","Products expose id, name, price and category","GET /api/products/{id} returns an existing product","GET /api/products/99999 returns 404","POST /api/products returns 201 with a generated id"]'
WHERE slug = 'challenge-1';
