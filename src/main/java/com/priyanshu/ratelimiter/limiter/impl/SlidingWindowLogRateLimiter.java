package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component("inMemorySlidingWindowLogRateLimiter")
public class SlidingWindowLogRateLimiter implements RateLimiter {

    private final ConcurrentHashMap<String, Queue<Long>> clientLogs = new ConcurrentHashMap<>();

    @Override
    public boolean allowRequest(String clientId, int limit, long windowSizeMillis) {
        long currentTime = System.currentTimeMillis();
        long threshold = currentTime - windowSizeMillis;

        Queue<Long> log = clientLogs.computeIfAbsent(clientId, k -> new ConcurrentLinkedQueue<>());

        synchronized (log) {
            // Remove timestamps older than threshold
            while (!log.isEmpty() && log.peek() < threshold) {
                log.poll();
            }

            if (log.size() < limit) {
                log.offer(currentTime);
                return true;
            }

            return false;
        }
    }
}
