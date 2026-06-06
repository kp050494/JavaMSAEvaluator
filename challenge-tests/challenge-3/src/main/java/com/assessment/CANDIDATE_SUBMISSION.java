package com.assessment;

import com.assessment.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ===========================================================================
 *  CHALLENGE 3 - Spring Data JPA Repository
 * ===========================================================================
 *  Reference implementation (replaced at runtime with candidate code).
 *  The candidate must declare a Spring Data repository for {@link Product}
 *  exposing derived query methods:
 *    - findByCategory(String category)  -> products in a category
 *    - findByPriceLessThan(Double price) -> products cheaper than a threshold
 *  The standard CRUD methods (save/findById/findAll) come from JpaRepository.
 * ===========================================================================
 */
interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    List<Product> findByPriceLessThan(Double price);
}
