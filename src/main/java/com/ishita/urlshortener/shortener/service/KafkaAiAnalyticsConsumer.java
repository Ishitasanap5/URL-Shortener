package com.ishita.urlshortener.shortener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ishita.urlshortener.shortener.dto.ClickEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaAiAnalyticsConsumer {

    private final ObjectMapper objectMapper;
    private final TrafficAnomalyEngine anomalyEngine;

    @KafkaListener(topics = "click-events", groupId = "url-analytics-group")
    public void analyzeTraffic(String message) {
        try {
            ClickEventMessage event = objectMapper.readValue(message, ClickEventMessage.class);
            double anomalyScore = anomalyEngine.calculateAnomalyScore(event);

            if (anomalyScore >= 0.75) {
                log.warn("🚨 System Security Alert: Bot/Click Fraud Detected! IP: {}, Score: {}",
                        event.getIpAddress(), anomalyScore);
                // System stands ready to write flag to DB or update Redis cache blocklist here
            }
        } catch (Exception ex) {
            log.error("Failed to evaluate traffic metrics asynchronously", ex);
        }
    }
}