package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component("redisFixedWindowRateLimiter")
public class RedisFixedWindowRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> script;

    public RedisFixedWindowRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>(
                "local current = redis.call('get', KEYS[1])\n" +
                "if current and tonumber(current) >= tonumber(ARGV[1]) then\n" +
                "    return 0\n" +
                "end\n" +
                "current = redis.call('incr', KEYS[1])\n" +
                "if tonumber(current) == 1 then\n" +
                "    redis.call('expire', KEYS[1], tonumber(ARGV[2]))\n" +
                "end\n" +
                "return 1", Long.class
        );
    }

    @Override
    public boolean allowRequest(String clientId, int limit, long windowSizeMillis) {
        String key = "ratelimit:fixed:" + clientId;
        long windowSeconds = Math.max(1, windowSizeMillis / 1000);
        Long result = redisTemplate.execute(script, Collections.singletonList(key), String.valueOf(limit), String.valueOf(windowSeconds));
        return result != null && result == 1;
    }
}
