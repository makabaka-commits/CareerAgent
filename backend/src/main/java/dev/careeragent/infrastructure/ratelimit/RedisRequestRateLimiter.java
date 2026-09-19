package dev.careeragent.infrastructure.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name="app.rate-limit.mode", havingValue="redis")
public class RedisRequestRateLimiter implements RequestRateLimiter {
    private final StringRedisTemplate redis;
    private final int limit;
    private final ConcurrentHashMap<String,Integer> fallback = new ConcurrentHashMap<>();
    public RedisRequestRateLimiter(StringRedisTemplate redis, @Value("${app.rate-limit.requests-per-minute:120}") int limit) {
        this.redis = redis; this.limit = limit;
    }
    @Override public boolean allow(String key) {
        String bucket = "careeragent:rate:" + Instant.now().getEpochSecond() / 60 + ":" + key;
        try {
            Long count = redis.opsForValue().increment(bucket);
            if (count != null && count == 1) redis.expire(bucket, Duration.ofSeconds(70));
            return count == null || count <= limit;
        } catch (Exception unavailable) {
            return fallback.merge(bucket, 1, Integer::sum) <= limit;
        }
    }
}
