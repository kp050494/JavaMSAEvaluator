package com.assessment;

import com.assessment.support.ResultPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Challenge 6 - Stateless JWT Security.
 *
 * Verifies the candidate's security layer:
 *   - POST /auth/login with valid credentials returns 200 + a token,
 *   - GET /api/products without a token returns 401,
 *   - GET /api/products with a valid Bearer token returns 200,
 *   - GET /api/products with an expired token returns 401.
 *
 * The expired token is forged here using the SAME secret declared in
 * src/main/resources/application.yml (security.jwt.secret).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ResultPrinter.class)
class JwtSecurityTest {

    /** Must match security.jwt.secret in application.yml. */
    private static final String SECRET =
            "assessment-challenge-6-super-secret-signing-key-please-change-0123456789";

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String loginAndGetToken() throws Exception {
        String body = "{\"username\":\"user\",\"password\":\"password\"}";
        String json = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        return node.get("token").asText();
    }

    @Test
    void login_withValidCredentials_returns200AndToken() throws Exception {
        String body = "{\"username\":\"user\",\"password\":\"password\"}";
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withValidToken_returns200() throws Exception {
        String token = loginAndGetToken();
        mockMvc.perform(get("/api/products").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void protectedEndpoint_withExpiredToken_returns401() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        String expired = Jwts.builder()
                .subject("user")
                .issuedAt(new Date(now - 7_200_000))
                .expiration(new Date(now - 3_600_000))
                .signWith(key)
                .compact();

        mockMvc.perform(get("/api/products").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withBadCredentials_isRejected() throws Exception {
        String body = "{\"username\":\"user\",\"password\":\"wrong\"}";
        int statusCode = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getStatus();
        assertThat(statusCode).isIn(401, 403);
    }
}
