package dev.careeragent.infrastructure.ratelimit;

public interface RequestRateLimiter {
    boolean allow(String key);
}
