package com.priyanshu.ratelimiter.exception;

public class RateLimitExceededException extends RuntimeException {

    private final int limit;
    private final long windowSeconds;
    private final long retryAfterSeconds;

    public RateLimitExceededException(int limit, long windowSeconds, long retryAfterSeconds) {
        super(String.format("Rate limit of %d requests per %d seconds exceeded. Try again in %d seconds.", limit, windowSeconds, retryAfterSeconds));
        this.limit = limit;
        this.windowSeconds = windowSeconds;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getLimit() {
        return limit;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
