package com.assessment;

import com.assessment.model.Product;
import com.assessment.support.ResultPrinter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Challenge 3 - Spring Data JPA Repository.
 *
 * Boots a JPA slice against an embedded H2 database and verifies that the
 * candidate's repository:
 *   - persists and retrieves entities (CRUD from JpaRepository),
 *   - filters by category via a derived query,
 *   - filters by price via a derived query,
 *   - is backed by a real (H2) schema created from the @Entity mapping.
 */
@DataJpaTest
@ExtendWith(ResultPrinter.class)
class ProductRepositoryTest {

    @Autowired
    ProductRepository repository;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        repository.save(new Product("Mechanical Keyboard", 89.99, "Peripherals"));
        repository.save(new Product("USB-C Hub", 34.00, "Peripherals"));
        repository.save(new Product("27\" Monitor", 199.50, "Displays"));
    }

    @Test
    void savesAndFindsById() {
        Product saved = repository.save(new Product("Webcam", 59.0, "Peripherals"));
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void findByCategory_returnsOnlyMatching() {
        List<Product> peripherals = repository.findByCategory("Peripherals");
        assertThat(peripherals)
                .hasSize(2)
                .allMatch(p -> p.getCategory().equals("Peripherals"));
    }

    @Test
    void findByPriceLessThan_filtersCorrectly() {
        List<Product> cheap = repository.findByPriceLessThan(100.0);
        assertThat(cheap)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Mechanical Keyboard", "USB-C Hub");
    }

    @Test
    void schemaCreated_findAllReturnsSeededRows() {
        assertThat(repository.findAll()).hasSize(3);
    }
}
