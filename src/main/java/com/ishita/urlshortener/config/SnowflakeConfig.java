package com.ishita.urlshortener.config;

import com.ishita.urlshortener.util.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfig {

    // You can change this per server instance later
    private static final long MACHINE_ID = 1;

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        return new SnowflakeIdGenerator(MACHINE_ID);
    }
}