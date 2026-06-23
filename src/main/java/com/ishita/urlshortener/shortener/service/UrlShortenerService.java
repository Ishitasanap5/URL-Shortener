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

    // INSERT-FIRST: no pre-check SELECT, dedup handled by DB constraint
    @Transactional
    public ShortenResponse shortenUrl(String longUrl) {
        log.info("Shortening URL: {}", longUrl);
        return createShortUrl(longUrl);
    }

    private ShortenResponse createShortUrl(String longUrl) {
        try {
            long id = snowflakeIdGenerator.generateId();
            String shortCode = Base62Encoder.encode(id);

            Url url = Url.builder()
                    .id(id)
                    .shortCode(shortCode)
                    .longUrl(longUrl)
                    .createdAt(Instant.now())
                    .build();

            urlRepository.saveAndFlush(url);
            bloomFilter.add(shortCode);

            log.info("Created shortCode={} for longUrl={}", shortCode, longUrl);

            return new ShortenResponse(
                    appConfig.getBaseUrl() + shortCode,
                    shortCode
            );

        } catch (DataIntegrityViolationException ex) {
            // Concurrent insert for the same longUrl — DB constraint fired.
            // Fetch the winner's record and return it.
            log.warn("Duplicate insert detected for longUrl={}, fetching existing", longUrl);

            Url existing = urlRepository.findByLongUrl(longUrl)
                    .orElseThrow(() -> new IllegalStateException(
                            "DataIntegrityViolationException on insert but longUrl not found " +
                                    "— constraint may have fired on shortCode collision, not longUrl. " +
                                    "longUrl=" + longUrl
                    ));

            return new ShortenResponse(
                    appConfig.getBaseUrl() + existing.getShortCode(),
                    existing.getShortCode()
            );
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
}