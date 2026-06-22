package com.ishita.urlshortener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    //ObjectMapper to resolve KafkaConsumerService's requirement
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Register the JavaTimeModule so it handles your Instant fields without failing
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}