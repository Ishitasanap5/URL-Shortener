package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.shortener.dto.ClickEventMessage;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class TrafficAnomalyEngine {

    private static final Set<String> BOT_UA_SIGNALS =
            Set.of("python", "curl", "wget", "httpclient", "go-http", "java/");

    public double calculateAnomalyScore(ClickEventMessage event) {
        double score = 0.0;

        String ua = Optional.ofNullable(event.getUserAgent())
                .map(String::toLowerCase)
                .orElse("");

        if (ua.isBlank()) {
            score += 0.40; // missing UA — strong bot signal
        } else if (BOT_UA_SIGNALS.stream().anyMatch(ua::contains)) {
            score += 0.60; // known scripted client
        }

        if (event.getReferrer() == null || event.getReferrer().isBlank()) {
            score += 0.15;
        }

        return Math.min(score, 1.0);
    }
}