package com.priyanshu.ratelimiter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class RateLimiterServiceApplicationTests {

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
    }

}
