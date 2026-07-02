package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component("inMemoryLeakyBucketRateLimiter")
public class LeakyBucketRateLimiter implements RateLimiter {

    private final ConcurrentHashMap<String, BucketData> clientBuckets = new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String clientId, int limit, long windowSizeMillis) {
        long currentTime = System.currentTimeMillis();
        BucketData bucket = clientBuckets.computeIfAbsent(clientId, k -> new BucketData(currentTime));

        synchronized (bucket) {
            long elapsed = currentTime - bucket.lastLeakTime;
            double leakedWater = elapsed * ((double) limit / windowSizeMillis);
            bucket.waterLevel = Math.max(0.0, bucket.waterLevel - leakedWater);
            bucket.lastLeakTime = currentTime;

            if (bucket.waterLevel + 1.0 <= limit) {
                bucket.waterLevel += 1.0;
                return true;
            }

            return false;
        }
    }

    private static class BucketData {
        double waterLevel;
        long lastLeakTime;

        BucketData(long currentTime) {
            this.waterLevel = 0.0;
            this.lastLeakTime = currentTime;
        }
    }
}
