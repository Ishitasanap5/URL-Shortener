package com.ishita.urlshortener.config;

import com.ishita.urlshortener.util.BloomFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomConfig {

    @Bean
    public BloomFilter bloomFilter() {
        return new BloomFilter(1_000_000);
    }
}