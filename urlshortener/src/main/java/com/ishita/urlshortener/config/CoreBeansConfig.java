package com.ishita.urlshortener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ishita.urlshortener.util.BloomFilter;
import com.ishita.urlshortener.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreBeansConfig {

    @Bean
    public BloomFilter bloomFilter(
            @Value("${app.bloom.expected-elements:1000000}") long expectedElements,
            @Value("${app.bloom.false-positive-rate:0.01}") double falsePositiveRate
    ) {
        return new BloomFilter(expectedElements, falsePositiveRate);
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
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}