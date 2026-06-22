package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.shortener.dto.ClickEventMessage;
import org.springframework.stereotype.Component;

@Component
public class TrafficAnomalyEngine {

    public double calculateAnomalyScore(ClickEventMessage event) {
        double score = 0.0;

        // 1. Check for headless or scripted bot User-Agents
        String ua = event.getUserAgent() != null ? event.getUserAgent().toLowerCase() : "";
        if (ua.contains("python") || ua.contains("curl") || ua.contains("wget") || ua.contains("httpclient")) {
            score += 0.60;
        }

        // 2. Missing core browser metadata (classic bot indicator)
        if (event.getUserAgent() == null || event.getUserAgent().isEmpty()) {
            score += 0.40;
        }
        if (event.getReferrer() == null || event.getReferrer().isEmpty()) {
            score += 0.15;
        }

        return Math.min(score, 1.0); // Cap at 1.0
    }
}