package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component("redisSlidingWindowLogRateLimiter")
public class RedisSlidingWindowLogRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> script;

    public RedisSlidingWindowLogRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>(
                "local key = KEYS[1]\n" +
                "local now = tonumber(ARGV[1])\n" +
                "local window = tonumber(ARGV[2])\n" +
                "local limit = tonumber(ARGV[3])\n" +
                "local member = ARGV[4]\n" +
                "local clearBefore = now - window\n" +
                "redis.call('zremrangebyscore', key, 0, clearBefore)\n" +
                "local count = redis.call('zcard', key)\n" +
                "if count < limit then\n" +
                "    redis.call('zadd', key, now, member)\n" +
                "    redis.call('expire', key, math.ceil(window / 1000))\n" +
                "    return 1\n" +
                "else\n" +
                "    return 0\n" +
                "end", Long.class
        );
    }

    @Override
    public boolean allowRequest(String clientId, int limit, long windowSizeMillis) {
        String key = "ratelimit:slidinglog:" + clientId;
        long now = System.currentTimeMillis();
        String member = now + ":" + UUID.randomUUID().toString();
        Long result = redisTemplate.execute(script,
                Collections.singletonList(key),
                String.valueOf(now),
                String.valueOf(windowSizeMillis),
                String.valueOf(limit),
                member
        );
        return result != null && result == 1;
    }
}
