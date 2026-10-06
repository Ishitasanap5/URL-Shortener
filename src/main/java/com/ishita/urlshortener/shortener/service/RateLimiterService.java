package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.cache.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final RedisService redisService;

    private static final int MAX_REQUESTS = 100;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String KEY_PREFIX = "rate_limit:";

    public boolean isAllowed(String ip) {

        String key = KEY_PREFIX + ip;

        Long count = redisService.increment(key);

        if (count == null) {
            // Redis returned null — fail open to avoid blocking all traffic
            // on a Redis outage. Log and monitor this.
            log.error("Redis returned null for increment on key={}. Failing open.", key);
            return true;
        }

        if (count == 1) {
            // First request in this window — set expiry.
            // This must happen immediately after the first increment,
            // not on every call, to avoid resetting the TTL on each request.
            redisService.expire(key, WINDOW);
        }

        if (count > MAX_REQUESTS) {
            log.warn("Rate limit exceeded for ip={}, count={}", ip, count);
            return false;
        }

        return true;
    }
}