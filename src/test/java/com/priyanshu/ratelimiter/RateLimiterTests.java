package com.priyanshu.ratelimiter;

import com.priyanshu.ratelimiter.limiter.impl.*;
import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTests {

    @Test
    void testFixedWindowRateLimiter() {
        RateLimiter limiter = new FixedWindowRateLimiter();
        String client = "client-fixed";

        // Allow up to 3 requests per 10 seconds
        assertTrue(limiter.allowRequest(client, 3, 10_000));
        assertTrue(limiter.allowRequest(client, 3, 10_000));
        assertTrue(limiter.allowRequest(client, 3, 10_000));
        assertFalse(limiter.allowRequest(client, 3, 10_000)); // 4th request rejected
    }

    @Test
    void testSlidingWindowLogRateLimiter() throws InterruptedException {
        RateLimiter limiter = new SlidingWindowLogRateLimiter();
        String client = "client-sliding-log";

        // Allow up to 2 requests per 1 second (1000ms)
        assertTrue(limiter.allowRequest(client, 2, 1000));
        assertTrue(limiter.allowRequest(client, 2, 1000));
        assertFalse(limiter.allowRequest(client, 2, 1000)); // 3rd request rejected

        // Wait 1.1 seconds for window to slide out
        Thread.sleep(1100);
        assertTrue(limiter.allowRequest(client, 2, 1000));
    }

    @Test
    void testSlidingWindowCounterRateLimiter() {
        RateLimiter limiter = new SlidingWindowCounterRateLimiter();
        String client = "client-sliding-counter";

        // Allow 3 requests per 1 minute (60_000ms)
        assertTrue(limiter.allowRequest(client, 3, 60_000));
        assertTrue(limiter.allowRequest(client, 3, 60_000));
        assertTrue(limiter.allowRequest(client, 3, 60_000));
        assertFalse(limiter.allowRequest(client, 3, 60_000));
    }

    @Test
    void testTokenBucketRateLimiter() throws InterruptedException {
        RateLimiter limiter = new TokenBucketRateLimiter();
        String client = "client-token-bucket";

        // Allow 2 requests per 1 second (capacity 2)
        assertTrue(limiter.allowRequest(client, 2, 1000));
        assertTrue(limiter.allowRequest(client, 2, 1000));
        assertFalse(limiter.allowRequest(client, 2, 1000));

        // Wait 500ms to refill 1 token (refill rate is 2 tokens/sec = 1 token/500ms)
        Thread.sleep(550);
        assertTrue(limiter.allowRequest(client, 2, 1000));
        assertFalse(limiter.allowRequest(client, 2, 1000));
    }

    @Test
    void testLeakyBucketRateLimiter() throws InterruptedException {
        RateLimiter limiter = new LeakyBucketRateLimiter();
        String client = "client-leaky-bucket";

        // Capacity 2, leaks at rate 2 requests per 1 second
        assertTrue(limiter.allowRequest(client, 2, 1000));
        assertTrue(limiter.allowRequest(client, 2, 1000));
        assertFalse(limiter.allowRequest(client, 2, 1000));

        // Wait 500ms so 1 unit of water leaks out
        Thread.sleep(550);
        assertTrue(limiter.allowRequest(client, 2, 1000));
    }
}
