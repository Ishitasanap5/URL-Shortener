package com.ishita.urlshortener.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "url:";
    private static final Duration TTL = Duration.ofDays(7);

    public void set(String shortCode, String longUrl) {
        redisTemplate.opsForValue()
                .set(PREFIX + shortCode, longUrl, TTL);
    }

    public String get(String shortCode) {
        return redisTemplate.opsForValue()
                .get(PREFIX + shortCode);
    }
}