package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component("inMemorySlidingWindowCounterRateLimiter")
public class SlidingWindowCounterRateLimiter implements RateLimiter {

    private final ConcurrentHashMap<String, CounterData> clientCounters = new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String clientId, int limit, long windowSizeMillis) {
        long currentTime = System.currentTimeMillis();
        CounterData data = clientCounters.computeIfAbsent(clientId, k -> new CounterData(currentTime, windowSizeMillis));

        synchronized (data) {
            long currentWindowStart = currentTime - (currentTime % windowSizeMillis);

            if (currentWindowStart > data.windowStart) {
                if (currentWindowStart == data.windowStart + windowSizeMillis) {
                    data.previousCount = data.currentCount;
                } else {
                    data.previousCount = 0;
                }
                data.currentCount = 0;
                data.windowStart = currentWindowStart;
            }

            long elapsed = currentTime - data.windowStart;
            double weight = (double) (windowSizeMillis - elapsed) / windowSizeMillis;
            double estimatedCount = data.previousCount * weight + data.currentCount;

            if (estimatedCount + 1 <= limit) {
                data.currentCount++;
                return true;
            }

            return false;
        }
    }

    private static class CounterData {
        long windowStart;
        int currentCount;
        int previousCount;

        CounterData(long currentTime, long windowSizeMillis) {
            this.windowStart = currentTime - (currentTime % windowSizeMillis);
            this.currentCount = 0;
            this.previousCount = 0;
        }
    }
}
