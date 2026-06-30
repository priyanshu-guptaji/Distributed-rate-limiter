package com.ratelimiter.limiter.fixedwindow;

public class WindowData {

    private long windowStart;
    private int requestCount;

    public WindowData(long windowStart, int requestCount) {
        this.windowStart = windowStart;
        this.requestCount = requestCount;
    }

    public long getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(long windowStart) {
        this.windowStart = windowStart;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }
}