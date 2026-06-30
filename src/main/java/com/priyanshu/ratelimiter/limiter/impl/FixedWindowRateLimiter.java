package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;

public class FixedWindowRateLimiter implements RateLimiter {

    @Override
    public boolean allowRequest(String clientId) {

        return true;

    }

}