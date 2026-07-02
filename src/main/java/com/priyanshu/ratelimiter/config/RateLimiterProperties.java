package com.priyanshu.ratelimiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private boolean enabled = true;
    private LimiterType type = LimiterType.IN_MEMORY;
    private LimiterAlgorithm algorithm = LimiterAlgorithm.FIXED_WINDOW;
    private List<RateLimitRule> rules = new ArrayList<>();
    private RateLimitRule defaultRule = new RateLimitRule("default", 100, 60);

    public enum LimiterType {
        IN_MEMORY,
        REDIS
    }

    public enum LimiterAlgorithm {
        FIXED_WINDOW,
        SLIDING_WINDOW_LOG,
        SLIDING_WINDOW_COUNTER,
        TOKEN_BUCKET,
        LEAKY_BUCKET
    }

    @Data
    public static class RateLimitRule {
        private String path;
        private int limit;
        private long windowSeconds;

        public RateLimitRule() {}

        public RateLimitRule(String path, int limit, long windowSeconds) {
            this.path = path;
            this.limit = limit;
            this.windowSeconds = windowSeconds;
        }
    }
}
