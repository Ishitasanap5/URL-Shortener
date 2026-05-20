package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.shortener.dto.ShortenResponse;
import com.ishita.urlshortener.shortener.model.Url;
import com.ishita.urlshortener.shortener.repository.UrlRepository;
import com.ishita.urlshortener.util.Base62Encoder;
import com.ishita.urlshortener.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UrlShortenerService {

    private final UrlRepository urlRepository;

    public ShortenResponse shortenUrl(String longUrl) {

        // check if already exists
        return urlRepository.findByLongUrl(longUrl)
                .map(url -> new ShortenResponse(
                        "http://localhost:8080/" + url.getShortCode(),
                        url.getShortCode()
                ))
                .orElseGet(() -> {

                    long id = IdGenerator.generateId();

                    String shortCode = Base62Encoder.encode(id);

                    Url url = Url.builder()
                            .id(id)
                            .shortCode(shortCode)
                            .longUrl(longUrl)
                            .createdAt(Instant.now())
                            .clickCount(0L)
                            .build();

                    urlRepository.save(url);

                    return new ShortenResponse(
                            "http://localhost:8080/" + shortCode,
                            shortCode
                    );
                });
    }

    public String getOriginalUrl(String shortCode) {

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short URL not found"));

        // increment click count
        url.setClickCount(url.getClickCount() + 1);

        urlRepository.save(url);

        return url.getLongUrl();
    }
}