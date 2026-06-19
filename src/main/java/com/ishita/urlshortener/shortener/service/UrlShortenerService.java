package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.cache.RedisService;
import com.ishita.urlshortener.shortener.dto.ShortenResponse;
import com.ishita.urlshortener.shortener.exception.UrlNotFoundException;
import com.ishita.urlshortener.shortener.model.Url;
import com.ishita.urlshortener.shortener.repository.UrlRepository;
import com.ishita.urlshortener.util.Base62Encoder;
import com.ishita.urlshortener.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerService {

    private static final String BASE_URL = "http://localhost:8080/";

    private final UrlRepository urlRepository;
    private final RedisService redisService;
    private final ClickService clickService;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final AnalyticsService analyticsService;


    public ShortenResponse shortenUrl(String longUrl) {

        log.info("Creating short URL for: {}", longUrl);

        return urlRepository.findByLongUrl(longUrl)
                .map(existingUrl -> {

                    log.info(
                            "URL already shortened. Returning existing short code: {}",
                            existingUrl.getShortCode()
                    );

                    return new ShortenResponse(
                            BASE_URL + existingUrl.getShortCode(),
                            existingUrl.getShortCode()
                    );
                })
                .orElseGet(() -> {

                    long id = snowflakeIdGenerator.generateId();

                    String shortCode = Base62Encoder.encode(id);

                    Url url = Url.builder()
                            .id(id)
                            .shortCode(shortCode)
                            .longUrl(longUrl)
                            .createdAt(Instant.now())
                            .clickCount(0L)
                            .build();

                    urlRepository.save(url);

                    log.info("Short URL created successfully: {}", shortCode);

                    return new ShortenResponse(
                            BASE_URL + shortCode,
                            shortCode
                    );
                });
    }

    public String getOriginalUrl(String shortCode) {

        log.info("Resolving shortCode: {}", shortCode);

        // 1. CHECK CACHE FIRST
        String cachedUrl = redisService.get(shortCode);

        if (cachedUrl != null) {

            log.info("Cache HIT for {}", shortCode);

            executorService.submit(() ->

                    clickService.incrementClick(shortCode)

            );

            return cachedUrl;
        }

        log.info("Cache MISS for {}", shortCode);

        // 2. FALLBACK TO DATABASE
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {

                    log.error("Short URL not found: {}", shortCode);

                    return new UrlNotFoundException(shortCode);
                });

        // 3. STORE IN CACHE
        redisService.set(shortCode, url.getLongUrl());

        // 4. UPDATE CLICK COUNT
        executorService.submit(() ->

                clickService.incrementClick(shortCode)

        );

        log.info("Redirecting to original URL: {}", url.getLongUrl());

        return url.getLongUrl();
    }

    private final ExecutorService executorService =
            Executors.newFixedThreadPool(10);
}