package dev.careeragent.infrastructure.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name="app.rate-limit.mode", havingValue="local", matchIfMissing=true)
public class LocalRequestRateLimiter implements RequestRateLimiter {
    private record Window(long minute, int count) {}
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int limit;
    public LocalRequestRateLimiter(@Value("${app.rate-limit.requests-per-minute:120}") int limit) { this.limit = limit; }
    @Override public boolean allow(String key) {
        long minute = Instant.now().getEpochSecond() / 60;
        Window value = windows.compute(key, (ignored, old) -> old == null || old.minute != minute
                ? new Window(minute, 1) : new Window(minute, old.count + 1));
        return value.count <= limit;
    }
}
