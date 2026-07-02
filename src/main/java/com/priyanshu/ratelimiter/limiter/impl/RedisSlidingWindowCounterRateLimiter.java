package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("redisSlidingWindowCounterRateLimiter")
public class RedisSlidingWindowCounterRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> script;

    public RedisSlidingWindowCounterRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>(
                "local current_key = KEYS[1]\n" +
                "local previous_key = KEYS[2]\n" +
                "local now = tonumber(ARGV[1])\n" +
                "local window = tonumber(ARGV[2])\n" +
                "local limit = tonumber(ARGV[3])\n" +
                "local current_count = tonumber(redis.call('get', current_key) or '0')\n" +
                "local previous_count = tonumber(redis.call('get', previous_key) or '0')\n" +
                "local current_window_start = now - (now % window)\n" +
                "local elapsed = now - current_window_start\n" +
                "local weight = (window - elapsed) / window\n" +
                "local estimated_count = previous_count * weight + current_count\n" +
                "if estimated_count + 1 <= limit then\n" +
                "    redis.call('incr', current_key)\n" +
                "    redis.call('expire', current_key, math.ceil((window * 2) / 1000))\n" +
                "    return 1\n" +
                "else\n" +
                "    return 0\n" +
                "end", Long.class
        );
    }

    @Override
    public boolean allowRequest(String clientId, int limit, long windowSizeMillis) {
        long now = System.currentTimeMillis();
        long currentWindowStart = now - (now % windowSizeMillis);
        long previousWindowStart = currentWindowStart - windowSizeMillis;

        String currentKey = "ratelimit:slidingcounter:" + clientId + ":" + currentWindowStart;
        String previousKey = "ratelimit:slidingcounter:" + clientId + ":" + previousWindowStart;

        Long result = redisTemplate.execute(script,
                Arrays.asList(currentKey, previousKey),
                String.valueOf(now),
                String.valueOf(windowSizeMillis),
                String.valueOf(limit)
        );
        return result != null && result == 1;
    }
}
