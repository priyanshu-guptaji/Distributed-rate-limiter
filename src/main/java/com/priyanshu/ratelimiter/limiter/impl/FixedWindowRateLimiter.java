package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import com.priyanshu.ratelimiter.model.WindowData;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component("inMemoryFixedWindowRateLimiter")
public class FixedWindowRateLimiter implements RateLimiter {

    private final ConcurrentHashMap<String, WindowData> userWindows = new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String clientId, int limit, long windowSizeMillis) {
        long currentTime = System.currentTimeMillis();

        WindowData updatedData = userWindows.compute(clientId, (key, data) -> {
            if (data == null) {
                return new WindowData(currentTime, 1);
            }

            if (currentTime - data.getWindowStart() >= windowSizeMillis) {
                data.setWindowStart(currentTime);
                data.setRequestCount(1);
                return data;
            }

            data.setRequestCount(data.getRequestCount() + 1);
            return data;
        });

        return updatedData.getRequestCount() <= limit;
    }
}