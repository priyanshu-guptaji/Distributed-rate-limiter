package com.priyanshu.ratelimiter.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordRequest(String path, String clientId, String status, String algorithm) {
        meterRegistry.counter("rate_limiter_requests_total",
                "path", path,
                "client_id", clientId,
                "status", status,
                "algorithm", algorithm
        ).increment();
    }
}
