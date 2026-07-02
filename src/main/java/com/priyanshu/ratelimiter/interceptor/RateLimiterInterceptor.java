package com.priyanshu.ratelimiter.interceptor;

import com.priyanshu.ratelimiter.config.RateLimiterProperties;
import com.priyanshu.ratelimiter.exception.RateLimitExceededException;
import com.priyanshu.ratelimiter.service.MetricsService;
import com.priyanshu.ratelimiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;
    private final RateLimiterProperties properties;
    private final MetricsService metricsService;

    public RateLimiterInterceptor(RateLimiterService rateLimiterService,
                                  RateLimiterProperties properties,
                                  MetricsService metricsService) {
        this.rateLimiterService = rateLimiterService;
        this.properties = properties;
        this.metricsService = metricsService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!properties.isEnabled()) {
            return true;
        }

        String path = request.getRequestURI();

        // Skip static docs/swagger endpoints from rate-limiting to make development/testing simple
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/actuator")) {
            return true;
        }

        // Find matching rule or use default
        RateLimiterProperties.RateLimitRule rule = properties.getRules().stream()
                .filter(r -> path.startsWith(r.getPath()))
                .findFirst()
                .orElse(properties.getDefaultRule());

        // Extract client ID
        String clientId = extractClientId(request);
        String limitKey = path + ":" + clientId;

        long windowMillis = rule.getWindowSeconds() * 1000;
        boolean allowed = rateLimiterService.allow(limitKey, rule.getLimit(), windowMillis);

        String algoName = properties.getAlgorithm().name();

        if (allowed) {
            metricsService.recordRequest(path, clientId, "ALLOWED", algoName);
            return true;
        } else {
            metricsService.recordRequest(path, clientId, "BLOCKED", algoName);
            throw new RateLimitExceededException(rule.getLimit(), rule.getWindowSeconds(), rule.getWindowSeconds());
        }
    }

    private String extractClientId(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return apiKey.trim();
        }

        String userId = request.getHeader("X-User-ID");
        if (userId != null && !userId.trim().isEmpty()) {
            return userId.trim();
        }

        return request.getRemoteAddr();
    }
}