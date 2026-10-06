package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.cache.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private RateLimiterService rateLimiterService;

    @Test
    void allowsRequestWhenUnderLimit() {
        when(redisService.increment(anyString())).thenReturn(1L);

        assertTrue(rateLimiterService.isAllowed("1.2.3.4"));
        verify(redisService).expire(anyString(), any());
    }

    @Test
    void blocksRequestOverLimit() {
        when(redisService.increment(anyString())).thenReturn(101L);

        assertFalse(rateLimiterService.isAllowed("1.2.3.4"));
    }

    @Test
    void failsOpenWhenRedisReturnsNull() {
        when(redisService.increment(anyString())).thenReturn(null);

        assertTrue(rateLimiterService.isAllowed("1.2.3.4"));
    }

    @Test
    void failsOpenWhenRedisThrows() {
        // This is the bug fix: a connection failure used to propagate as an
        // uncaught exception (every request 500ing during a Redis outage)
        // instead of failing open like the null-return case already did.
        when(redisService.increment(anyString()))
                .thenThrow(new RedisConnectionFailureException("connection refused"));

        assertTrue(rateLimiterService.isAllowed("1.2.3.4"));
    }
}
