package com.ishita.urlshortener.config;

import com.ishita.urlshortener.shortener.model.Url;
import com.ishita.urlshortener.shortener.repository.UrlRepository;
import com.ishita.urlshortener.util.BloomFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BloomStartupLoader {

    private final UrlRepository urlRepository;
    private final BloomFilter bloomFilter;

    @Bean
    public ApplicationRunner loadBloomFilter() {
        return args -> {

            System.out.println("Rehydrating Bloom Filter from DB...");

            for (Url url : urlRepository.findAll()) {
                bloomFilter.add(url.getShortCode());
            }

            System.out.println("Bloom Filter loaded with existing URLs.");
        };
    }
}