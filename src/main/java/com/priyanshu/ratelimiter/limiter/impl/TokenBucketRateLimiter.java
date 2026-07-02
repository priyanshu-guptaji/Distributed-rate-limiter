package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component("inMemoryTokenBucketRateLimiter")
public class TokenBucketRateLimiter implements RateLimiter {

    private final ConcurrentHashMap<String, BucketData> clientBuckets = new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String clientId, int limit, long windowSizeMillis) {
        long currentTime = System.currentTimeMillis();
        BucketData bucket = clientBuckets.computeIfAbsent(clientId, k -> new BucketData(currentTime, limit));

        synchronized (bucket) {
            long elapsed = currentTime - bucket.lastRefillTime;
            double tokensToAdd = elapsed * ((double) limit / windowSizeMillis);
            bucket.tokens = Math.min(limit, bucket.tokens + tokensToAdd);
            bucket.lastRefillTime = currentTime;

            if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0;
                return true;
            }

            return false;
        }
    }

    private static class BucketData {
        double tokens;
        long lastRefillTime;

        BucketData(long currentTime, double capacity) {
            this.tokens = capacity;
            this.lastRefillTime = currentTime;
        }
    }
}
