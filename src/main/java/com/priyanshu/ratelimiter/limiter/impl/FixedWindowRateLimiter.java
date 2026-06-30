package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class FixedWindowRateLimiter implements RateLimiter {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_SIZE = 60_000;

    private final ConcurrentHashMap<String, com.ratelimiter.limiter.fixedwindow.WindowData> userWindows =
            new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String userId) {

        long currentTime = System.currentTimeMillis();

        com.ratelimiter.limiter.fixedwindow.WindowData updatedData = userWindows.compute(userId, (key, data) -> {

            if (data == null) {
                System.out.println("New User");
                return new com.ratelimiter.limiter.fixedwindow.WindowData(currentTime, 1);
            }

            if (currentTime - data.getWindowStart() >= WINDOW_SIZE) {
                System.out.println("Window Reset");
                data.setWindowStart(currentTime);
                data.setRequestCount(1);
                return data;
            }

            data.setRequestCount(data.getRequestCount() + 1);

            System.out.println("Count = " + data.getRequestCount());

            return data;
        });

        boolean allowed = updatedData.getRequestCount() <= MAX_REQUESTS;

        System.out.println("Allowed = " + allowed);
        System.out.println("----------------");

        return allowed;
    }
}