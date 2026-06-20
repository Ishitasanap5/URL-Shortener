package com.ishita.urlshortener.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

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
    public Long increment(String key) {

        return redisTemplate.opsForValue()
                .increment(key);
    }
    public void expire(String key, Duration duration){

        redisTemplate.expire(key, duration);

    }
}