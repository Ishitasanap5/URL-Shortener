package com.ishita.urlshortener.config;

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
}