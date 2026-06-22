package com.ishita.urlshortener.config;

import com.ishita.urlshortener.shortener.repository.UrlRepository;
import com.ishita.urlshortener.util.BloomFilter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BloomFilterLoader {

    private final UrlRepository urlRepository;
    private final BloomFilter bloomFilter;

    @PostConstruct
    public void loadExistingUrls() {

        urlRepository.findAll()
                .forEach(url -> bloomFilter.add(url.getShortCode()));
    }
}