package com.assessment;

import com.assessment.support.ResultPrinter;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Challenge 5 - Resilience (Circuit Breaker, Fallback, Timeout).
 *
 * A WireMock server stands in for the upstream product-service. The tests
 * verify that the candidate's resilient client:
 *   - passes real upstream data through on success,
 *   - returns the fallback payload when the upstream replies 503,
 *   - returns the fallback payload (without hanging) when the upstream is slow,
 *     proving the read timeout is honoured.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ResultPrinter.class)
class ResilientClientTest {

    private static final WireMockServer WIRE_MOCK = new WireMockServer(options().dynamicPort());

    @Autowired
    MockMvc mockMvc;

    @DynamicPropertySource
    static void upstreamProperties(DynamicPropertyRegistry registry) {
        if (!WIRE_MOCK.isRunning()) {
            WIRE_MOCK.start();
        }
        registry.add("upstream.base-url", () -> "http://localhost:" + WIRE_MOCK.port());
        // Comfortably above a cold-start HTTP call, but well below the slow stub's delay.
        registry.add("upstream.timeout-ms", () -> "3000");
    }

    @AfterAll
    static void stopWireMock() {
        WIRE_MOCK.stop();
    }

    @BeforeEach
    void resetStubs() {
        WIRE_MOCK.resetAll();
    }

    @Test
    void upstreamSuccess_passesDataThrough() throws Exception {
        WIRE_MOCK.stubFor(WireMock.get(WireMock.urlEqualTo("/products"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"Keyboard\",\"source\":\"upstream\"}]")));

        mockMvc.perform(get("/api/external/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Keyboard"))
                .andExpect(jsonPath("$[0].source").value("upstream"));
    }

    @Test
    void upstream503_firesFallback() throws Exception {
        WIRE_MOCK.stubFor(WireMock.get(WireMock.urlEqualTo("/products"))
                .willReturn(WireMock.aResponse().withStatus(503)));

        mockMvc.perform(get("/api/external/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].source").value("fallback"));
    }

    @Test
    void slowUpstream_timesOutToFallback() throws Exception {
        WIRE_MOCK.stubFor(WireMock.get(WireMock.urlEqualTo("/products"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(8000)
                        .withBody("[{\"name\":\"TooSlow\",\"source\":\"upstream\"}]")));

        long start = System.currentTimeMillis();
        mockMvc.perform(get("/api/external/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].source").value("fallback"));
        long elapsed = System.currentTimeMillis() - start;

        // The 3s client timeout must trip well before the 8s upstream delay.
        org.assertj.core.api.Assertions.assertThat(elapsed).isLessThan(6000);
    }
}
