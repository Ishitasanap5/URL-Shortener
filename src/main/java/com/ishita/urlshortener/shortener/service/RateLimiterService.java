package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.cache.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisService redisService;

    private static final int MAX_REQUESTS = 100;

    private static final Duration WINDOW =
            Duration.ofMinutes(1);

    public boolean isAllowed(String ip){

        String key = "rate_limit:" + ip;

        Long count =
                redisService.increment(key);

        if(count == 1){

            redisService.expire(key, WINDOW);

        }

        return count <= MAX_REQUESTS;

    }

}