package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component("redisTokenBucketRateLimiter")
public class RedisTokenBucketRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> script;

    public RedisTokenBucketRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>(
                "local key = KEYS[1]\n" +
                "local now = tonumber(ARGV[1])\n" +
                "local limit = tonumber(ARGV[2])\n" +
                "local window = tonumber(ARGV[3])\n" +
                "local data = redis.call('hmget', key, 'tokens', 'lastRefillTime')\n" +
                "local tokens = tonumber(data[1])\n" +
                "local lastRefillTime = tonumber(data[2])\n" +
                "if not tokens then\n" +
                "    tokens = limit\n" +
                "    lastRefillTime = now\n" +
                "else\n" +
                "    local elapsed = now - lastRefillTime\n" +
                "    local tokensToAdd = elapsed * (limit / window)\n" +
                "    tokens = math.min(limit, tokens + tokensToAdd)\n" +
                "    lastRefillTime = now\n" +
                "end\n" +
                "if tokens >= 1.0 then\n" +
                "    tokens = tokens - 1.0\n" +
                "    redis.call('hmset', key, 'tokens', tokens, 'lastRefillTime', lastRefillTime)\n" +
                "    redis.call('expire', key, math.ceil(window / 1000))\n" +
                "    return 1\n" +
                "else\n" +
                "    redis.call('hmset', key, 'tokens', tokens, 'lastRefillTime', lastRefillTime)\n" +
                "    redis.call('expire', key, math.ceil(window / 1000))\n" +
                "    return 0\n" +
                "end", Long.class
        );
    }

    @Override
    public boolean allowRequest(String clientId, int limit, long windowSizeMillis) {
        String key = "ratelimit:tokenbucket:" + clientId;
        long now = System.currentTimeMillis();
        Long result = redisTemplate.execute(script,
                Collections.singletonList(key),
                String.valueOf(now),
                String.valueOf(limit),
                String.valueOf(windowSizeMillis)
        );
        return result != null && result == 1;
    }
}
