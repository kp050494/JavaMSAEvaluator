package com.assessment;

import com.assessment.support.ResultPrinter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Challenge 2 - Service Layer + Bean Validation.
 *
 * Verifies that the candidate:
 *   - wires a @Service bean into the controller (present in the context),
 *   - enforces @Valid so a blank name returns 400,
 *   - enforces @Valid so a non-positive price returns 400,
 *   - returns 201 Created with a generated id on a valid POST.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ResultPrinter.class)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ApplicationContext context;

    @Test
    void serviceBean_isWiredIntoContext() {
        long serviceBeans = java.util.Arrays.stream(context.getBeanDefinitionNames())
                .map(name -> context.getType(name))
                .filter(type -> type != null && type.getName().endsWith("ProductService"))
                .count();
        assertThat(serviceBeans)
                .as("expected a ProductService @Service bean in the application context")
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    void validProduct_returns201Created() throws Exception {
        String body = "{\"name\":\"Wireless Mouse\",\"price\":24.99,\"category\":\"Peripherals\"}";
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Wireless Mouse"));
    }

    @Test
    void blankName_returns400() throws Exception {
        String body = "{\"name\":\"\",\"price\":10.0,\"category\":\"Peripherals\"}";
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nonPositivePrice_returns400() throws Exception {
        String body = "{\"name\":\"Broken Item\",\"price\":-5.0,\"category\":\"Peripherals\"}";
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void savedProduct_isPersistedAndListed() throws Exception {
        String body = "{\"name\":\"Desk Lamp\",\"price\":42.0,\"category\":\"Office\"}";
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == 'Desk Lamp')]").exists());
    }
}
