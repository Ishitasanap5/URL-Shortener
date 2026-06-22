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

    // CREATE SHORT URL
    public ShortenResponse shortenUrl(String longUrl) {

        log.info("Creating short URL for: {}", longUrl);

        return urlRepository.findByLongUrl(longUrl)
                .map(existingUrl -> new ShortenResponse(
                        appConfig.getBaseUrl() + existingUrl.getShortCode(),
                        existingUrl.getShortCode()
                ))
                .orElseGet(() -> {

                    long id = snowflakeIdGenerator.generateId();
                    String shortCode = Base62Encoder.encode(id);

                    Url url = Url.builder()
                            .id(id)
                            .shortCode(shortCode)
                            .longUrl(longUrl)
                            .createdAt(Instant.now())
                            .build();

                    urlRepository.save(url);

                    bloomFilter.add(shortCode);

                    log.info("Short URL created successfully: {}", shortCode);

                    return new ShortenResponse(
                            appConfig.getBaseUrl() + shortCode,
                            shortCode
                    );
                });
    }

    // REDIRECT LOGIC
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
}