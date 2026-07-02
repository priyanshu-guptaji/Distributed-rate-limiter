package com.priyanshu.ratelimiter.config;

import com.priyanshu.ratelimiter.limiter.strategy.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;

@Configuration
public class RateLimiterConfig {

    private final RateLimiterProperties properties;
    private final Map<String, RateLimiter> rateLimiters;

    public RateLimiterConfig(RateLimiterProperties properties, Map<String, RateLimiter> rateLimiters) {
        this.properties = properties;
        this.rateLimiters = rateLimiters;
    }

    @Bean
    @Primary
    public RateLimiter dynamicRateLimiter() {
        return (clientId, limit, windowSizeMillis) -> {
            String beanName = getLimiterBeanName();
            RateLimiter delegate = rateLimiters.get(beanName);
            if (delegate == null) {
                throw new IllegalStateException("No RateLimiter found with bean name: " + beanName);
            }
            return delegate.allowRequest(clientId, limit, windowSizeMillis);
        };
    }

    private String getLimiterBeanName() {
        String typePrefix = properties.getType() == RateLimiterProperties.LimiterType.REDIS ? "redis" : "inMemory";
        String algoSuffix = switch (properties.getAlgorithm()) {
            case FIXED_WINDOW -> "FixedWindowRateLimiter";
            case SLIDING_WINDOW_LOG -> "SlidingWindowLogRateLimiter";
            case SLIDING_WINDOW_COUNTER -> "SlidingWindowCounterRateLimiter";
            case TOKEN_BUCKET -> "TokenBucketRateLimiter";
            case LEAKY_BUCKET -> "LeakyBucketRateLimiter";
        };
        return typePrefix + algoSuffix;
    }
}
