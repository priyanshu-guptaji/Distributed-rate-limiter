package com.priyanshu.ratelimiter.limiter.strategy;

public interface RateLimiter {
    boolean allowRequest(String clientId);
}
