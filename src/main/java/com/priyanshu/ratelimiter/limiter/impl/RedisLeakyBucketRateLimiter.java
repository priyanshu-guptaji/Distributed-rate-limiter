package com.priyanshu.ratelimiter.limiter.impl;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component("redisLeakyBucketRateLimiter")
public class RedisLeakyBucketRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> script;

    public RedisLeakyBucketRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>(
                "local key = KEYS[1]\n" +
                "local now = tonumber(ARGV[1])\n" +
                "local limit = tonumber(ARGV[2])\n" +
                "local window = tonumber(ARGV[3])\n" +
                "local data = redis.call('hmget', key, 'waterLevel', 'lastLeakTime')\n" +
                "local waterLevel = tonumber(data[1])\n" +
                "local lastLeakTime = tonumber(data[2])\n" +
                "if not waterLevel then\n" +
                "    waterLevel = 0.0\n" +
                "    lastLeakTime = now\n" +
                "else\n" +
                "    local elapsed = now - lastLeakTime\n" +
                "    local leaked = elapsed * (limit / window)\n" +
                "    waterLevel = math.max(0.0, waterLevel - leaked)\n" +
                "    lastLeakTime = now\n" +
                "end\n" +
                "if waterLevel + 1.0 <= limit then\n" +
                "    waterLevel = waterLevel + 1.0\n" +
                "    redis.call('hmset', key, 'waterLevel', waterLevel, 'lastLeakTime', lastLeakTime)\n" +
                "    redis.call('expire', key, math.ceil(window / 1000))\n" +
                "    return 1\n" +
                "else\n" +
                "    redis.call('hmset', key, 'waterLevel', waterLevel, 'lastLeakTime', lastLeakTime)\n" +
                "    redis.call('expire', key, math.ceil(window / 1000))\n" +
                "    return 0\n" +
                "end", Long.class
        );
    }

    @Override
    public boolean allowRequest(String clientId, int limit, long windowSizeMillis) {
        String key = "ratelimit:leakybucket:" + clientId;
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
