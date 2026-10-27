package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.cache.RedisService;
import com.ishita.urlshortener.config.AppConfig;
import com.ishita.urlshortener.shortener.dto.ShortenResponse;
import com.ishita.urlshortener.shortener.exception.UrlNotFoundException;
import com.ishita.urlshortener.shortener.model.Url;
import com.ishita.urlshortener.shortener.repository.UrlRepository;
import com.ishita.urlshortener.util.Base62Encoder;
import com.ishita.urlshortener.util.BloomFilter;
import com.ishita.urlshortener.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerService {

    private final AppConfig appConfig;
    private final UrlRepository urlRepository;
    private final RedisService redisService;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final BloomFilter bloomFilter;

    public ShortenResponse shortenUrl(String longUrl) {
        log.info("Shortening URL: {}", longUrl);

        // Fast path: reuse an existing short URL (looked up by SHA-256 hash).
        String hash = Url.sha256(longUrl);

        return urlRepository.findByLongUrlHash(hash)
                .map(existing -> toShortenResponse(existing.getShortCode()))
                .orElseGet(() -> createShortUrl(longUrl));
    }

    private ShortenResponse createShortUrl(String longUrl) {
        long id = snowflakeIdGenerator.generateId();
        String shortCode = Base62Encoder.encode(id);

        Url url = Url.builder()
                .id(id)
                .shortCode(shortCode)
                .longUrl(longUrl)
                .longUrlHash(Url.sha256(longUrl))
                .createdAt(Instant.now())
                .build();

        try {

            urlRepository.saveAndFlush(url);

            bloomFilter.add(shortCode);

            log.info("Created shortCode={} for longUrl={}", shortCode, longUrl);

            return toShortenResponse(shortCode);

        } catch (DataIntegrityViolationException ex) {

            log.warn(
                    "Insert constraint violation for longUrl={}. Checking for an existing URL.",
                    longUrl
            );

            return urlRepository.findByLongUrlHash(Url.sha256(longUrl))
                    .map(existing -> toShortenResponse(existing.getShortCode()))
                    .orElseThrow(() -> ex);
        }
    }

    public String getOriginalUrl(String shortCode) {
        log.info("Resolving shortCode={}", shortCode);

        if (!bloomFilter.mightContain(shortCode)) {
            throw new UrlNotFoundException(shortCode);
        }

        String cached = redisService.get(shortCode);

        if (cached != null) {
            log.debug("Cache HIT for shortCode={}", shortCode);
            return cached;
        }

        log.debug("Cache MISS for shortCode={}", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        redisService.set(shortCode, url.getLongUrl());

        return url.getLongUrl();
    }

    private ShortenResponse toShortenResponse(String shortCode) {
        return new ShortenResponse(
                appConfig.getBaseUrl() + shortCode,
                shortCode
        );
    }
}