package com.ishita.urlshortener.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.ishita.urlshortener.shortener.model.Url;
import com.ishita.urlshortener.shortener.repository.UrlRepository;
import com.ishita.urlshortener.util.BloomFilter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BloomStartupLoader {

    private final UrlRepository urlRepository;
    private final BloomFilter bloomFilter;

    private static final int PAGE_SIZE = 1000;

    @Bean
    public ApplicationRunner loadBloomFilter() {
        return args -> {
            log.info("Rehydrating Bloom Filter from DB...");

            Pageable pageable = PageRequest.of(0, PAGE_SIZE);
            Page<Url> page;
            int total = 0;

            do {
                page = urlRepository.findAll(pageable);
                page.forEach(url -> bloomFilter.add(url.getShortCode()));
                total += page.getNumberOfElements();
                pageable = page.nextPageable();
            } while (page.hasNext());

            log.info("Bloom Filter loaded with {} short codes.", total);
        };
    }
}