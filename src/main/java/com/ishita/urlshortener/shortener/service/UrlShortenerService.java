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
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional
    public ShortenResponse shortenUrl(String longUrl) {

        log.info("Creating short URL for: {}", longUrl);

        return urlRepository.findByLongUrl(longUrl)
                .map(this::buildResponse)
                .orElseGet(() -> createNewShortUrl(longUrl));
    }


    private ShortenResponse createNewShortUrl(String longUrl) {

        try {
            long id = snowflakeIdGenerator.generateId();
            String shortCode = Base62Encoder.encode(id);

            Url url = Url.builder()
                    .id(id)
                    .shortCode(shortCode)
                    .longUrl(longUrl)
                    .createdAt(Instant.now())
                    .build();

            urlRepository.saveAndFlush(url); // IMPORTANT: immediate DB write

            bloomFilter.add(shortCode);

            log.info("Short URL created successfully: {}", shortCode);

            return buildResponse(url);

        } catch (DataIntegrityViolationException ex) {

            // Another thread already inserted same longUrl
            log.warn("Duplicate insert detected, fetching existing record");

            Url existing = urlRepository.findByLongUrl(longUrl)
                    .orElseThrow(() ->
                            new RuntimeException("Race condition resolved but record not found")
                    );

            return buildResponse(existing);
        }
    }


    public String getOriginalUrl(String shortCode) {

        log.info("Resolving shortCode: {}", shortCode);

        if (!bloomFilter.mightContain(shortCode)) {
            throw new UrlNotFoundException(shortCode);
        }

        String cachedUrl = redisService.get(shortCode);

        if (cachedUrl != null) {
            log.info("Cache HIT for {}", shortCode);
            return cachedUrl;
        }

        log.info("Cache MISS for {}", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        redisService.set(shortCode, url.getLongUrl());

        return url.getLongUrl();
    }


    private ShortenResponse buildResponse(Url url) {
        return new ShortenResponse(
                appConfig.getBaseUrl() + url.getShortCode(),
                url.getShortCode()
        );
    }
}