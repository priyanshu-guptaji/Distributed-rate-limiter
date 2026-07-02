package com.priyanshu.ratelimiter;

import com.priyanshu.ratelimiter.config.RateLimiterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class RateLimiterIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RateLimiterProperties properties;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        properties.setEnabled(true);
        properties.setType(RateLimiterProperties.LimiterType.IN_MEMORY);
        properties.setAlgorithm(RateLimiterProperties.LimiterAlgorithm.FIXED_WINDOW);

        // Configure rules: path "/hello" with limit 2 per 60 seconds
        RateLimiterProperties.RateLimitRule rule = new RateLimiterProperties.RateLimitRule("/hello", 2, 60);
        properties.setRules(Collections.singletonList(rule));
    }

    @Test
    void testRateLimitingEnforcement() throws Exception {
        // Request 1: Allowed
        mockMvc.perform(get("/hello").header("X-API-Key", "test-user"))
                .andExpect(status().isOk())
                .andExpect(content().string("Request Allowed! Welcome to the rate limited API."));

        // Request 2: Allowed
        mockMvc.perform(get("/hello").header("X-API-Key", "test-user"))
                .andExpect(status().isOk());

        // Request 3: Blocked (HTTP 429)
        mockMvc.perform(get("/hello").header("X-API-Key", "test-user"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.limit").value(2))
                .andExpect(jsonPath("$.windowSeconds").value(60));
    }
}
