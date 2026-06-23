package com.ishita.urlshortener.config;

import com.fasterxml.jackson.databind.ObjectMapper; // Use this import
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Essential for Instant
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ishita.urlshortener.util.BloomFilter;
import com.ishita.urlshortener.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreBeansConfig {

    @Bean
    public BloomFilter bloomFilter() {
        return new BloomFilter(1_000_000);
    }

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(
            @Value("${app.machine-id:1}") long machineId
    ) {
        return new SnowflakeIdGenerator(machineId);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register the time module so 'Instant' works
        mapper.registerModule(new JavaTimeModule());
        // Optional: keep timestamps human-readable if needed
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}