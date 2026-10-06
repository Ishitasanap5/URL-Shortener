package com.ishita.urlshortener.shortener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ishita.urlshortener.shortener.dto.ClickEventMessage;
import com.ishita.urlshortener.shortener.model.ClickEvent;
import com.ishita.urlshortener.shortener.repository.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final ClickEventRepository clickEventRepository;
    private final ObjectMapper objectMapper;
    private final TrafficAnomalyEngine anomalyEngine;

    @KafkaListener(
            topics = "click-events",
            containerFactory = "kafkaListenerContainerFactory"
            // groupId removed — inherited from ConsumerFactory via KafkaConfig
    )
    public void consume(String message) {
        try {
            ClickEventMessage event = objectMapper.readValue(message, ClickEventMessage.class);

            double score = anomalyEngine.calculateAnomalyScore(event);
            if (score >= 0.75) {
                log.warn(
                        "Anomaly detected — shortCode={}, ip={}, score={}",
                        event.getShortCode(), event.getIpAddress(), score
                );
                // Not returning here intentionally: still persist the event
                // so analytics counts remain accurate; anomaly is flagged in logs
            }

            ClickEvent entity = ClickEvent.builder()
                    .shortCode(event.getShortCode())
                    .clickedAt(event.getClickedAt())
                    .ipAddress(event.getIpAddress())
                    .userAgent(event.getUserAgent())
                    .referrer(event.getReferrer())
                    .build();

            clickEventRepository.save(entity);

        } catch (Exception ex) {
            // Logging the raw message helps replay/debug from dead-letter topic
            log.error("Failed to process Kafka message: {}", message, ex);
            // DefaultErrorHandler in KafkaConfig will retry 2x with 1s backoff
            // before routing to dead-letter if configured
            throw new RuntimeException(ex);
            // Re-throw so Spring's error handler gets it and retries/DLQs.
            // Swallowing here would silently ack a bad message.
        }
    }
}