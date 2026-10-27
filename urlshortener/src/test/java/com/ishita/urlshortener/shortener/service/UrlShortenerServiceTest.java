package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.cache.RedisService;
import com.ishita.urlshortener.config.AppConfig;
import com.ishita.urlshortener.shortener.dto.ShortenResponse;
import com.ishita.urlshortener.shortener.exception.UrlNotFoundException;
import com.ishita.urlshortener.shortener.model.Url;
import com.ishita.urlshortener.shortener.repository.UrlRepository;
import com.ishita.urlshortener.util.BloomFilter;
import com.ishita.urlshortener.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private AppConfig appConfig;
    @Mock
    private UrlRepository urlRepository;
    @Mock
    private RedisService redisService;
    @Mock
    private SnowflakeIdGenerator snowflakeIdGenerator;
    @Mock
    private BloomFilter bloomFilter;

    @InjectMocks
    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        lenient().when(appConfig.getBaseUrl()).thenReturn("http://short.test/");
    }

    @Test
    void shortenUrl_happyPath_savesAndReturnsShortUrl() {
        when(snowflakeIdGenerator.generateId()).thenReturn(1000L);

        ShortenResponse response = service.shortenUrl("https://example.com/very/long/path");

        assertEquals("http://short.test/" + response.shortCode(), response.shortUrl());
        verify(urlRepository).saveAndFlush(any(Url.class));
        verify(bloomFilter).add(response.shortCode());
    }

    @Test
    void shortenUrl_existingUrl_returnsExistingWithoutInserting() {
        String longUrl = "https://example.com/already";
        Url existing = Url.builder()
                .id(1L)
                .shortCode("existingCode")
                .longUrl(longUrl)
                .createdAt(Instant.now())
                .build();
        when(urlRepository.findByLongUrlHash(Url.sha256(longUrl)))
                .thenReturn(Optional.of(existing));

        ShortenResponse response = service.shortenUrl(longUrl);

        assertEquals("existingCode", response.shortCode());
        verify(urlRepository, never()).saveAndFlush(any(Url.class));
        verifyNoInteractions(snowflakeIdGenerator);
    }

    @Test
    void shortenUrl_concurrentDuplicateInsert_returnsExistingRecord() {
        String longUrl = "https://example.com/dup";
        when(snowflakeIdGenerator.generateId()).thenReturn(2000L);
        when(urlRepository.saveAndFlush(any(Url.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint violated"));

        Url existing = Url.builder()
                .id(1L)
                .shortCode("existingCode")
                .longUrl(longUrl)
                .createdAt(Instant.now())
                .build();
        when(urlRepository.findByLongUrlHash(Url.sha256(longUrl)))
                .thenReturn(Optional.empty(), Optional.of(existing));

        ShortenResponse response = service.shortenUrl(longUrl);

        assertEquals("existingCode", response.shortCode());
        assertEquals("http://short.test/existingCode", response.shortUrl());
        // The bloom filter should not gain a phantom entry for a code that
        // was never actually persisted by this call.
        verify(bloomFilter, never()).add(anyString());
    }

    @Test
    void shortenUrl_duplicateInsertButNoMatchingLongUrl_rethrowsOriginalException() {
        String longUrl = "https://example.com/edge-case";
        when(snowflakeIdGenerator.generateId()).thenReturn(3000L);
        when(urlRepository.saveAndFlush(any(Url.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint violated"));
        when(urlRepository.findByLongUrlHash(Url.sha256(longUrl)))
                .thenReturn(Optional.empty());

        assertThrows(DataIntegrityViolationException.class, () -> service.shortenUrl(longUrl));
    }

    @Test
    void getOriginalUrl_bloomFilterSaysAbsent_shortCircuitsWithoutHittingRedisOrDb() {
        when(bloomFilter.mightContain("nope")).thenReturn(false);

        assertThrows(UrlNotFoundException.class, () -> service.getOriginalUrl("nope"));

        verifyNoInteractions(redisService);
        verifyNoInteractions(urlRepository);
    }

    @Test
    void getOriginalUrl_cacheHit_returnsCachedValueWithoutHittingDb() {
        when(bloomFilter.mightContain("abc123")).thenReturn(true);
        when(redisService.get("abc123")).thenReturn("https://example.com/cached");

        String result = service.getOriginalUrl("abc123");

        assertEquals("https://example.com/cached", result);
        verifyNoInteractions(urlRepository);
    }

    @Test
    void getOriginalUrl_cacheMiss_fallsBackToDbAndRepopulatesCache() {
        when(bloomFilter.mightContain("abc123")).thenReturn(true);
        when(redisService.get("abc123")).thenReturn(null);

        Url url = Url.builder()
                .id(1L)
                .shortCode("abc123")
                .longUrl("https://example.com/from-db")
                .createdAt(Instant.now())
                .build();
        when(urlRepository.findByShortCode("abc123")).thenReturn(Optional.of(url));

        String result = service.getOriginalUrl("abc123");

        assertEquals("https://example.com/from-db", result);
        verify(redisService).set("abc123", "https://example.com/from-db");
    }

    @Test
    void getOriginalUrl_bloomFilterFalsePositive_dbMiss_throwsNotFound() {
        when(bloomFilter.mightContain("ghost")).thenReturn(true);
        when(redisService.get("ghost")).thenReturn(null);
        when(urlRepository.findByShortCode("ghost")).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> service.getOriginalUrl("ghost"));
    }
}