package com.ishita.urlshortener.shortener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ishita.urlshortener.shortener.dto.ClickEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "click-events";

    public void sendClickEvent(ClickEventMessage event) {
        try {
            String json = objectMapper.writeValueAsString(event);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(TOPIC, event.getShortCode(), json);
            // Keying by shortCode ensures all clicks for one URL
            // go to the same partition — preserves ordering per URL

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error(
                            "Failed to publish click event for shortCode={}: {}",
                            event.getShortCode(), ex.getMessage()
                    );
                    // TODO: write to fallback outbox table if
                    // analytics durability is required
                } else {
                    log.debug(
                            "Click event published for shortCode={}, partition={}, offset={}",
                            event.getShortCode(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                }
            });

        } catch (Exception e) {
            log.error(
                    "Failed to serialize click event for shortCode={}",
                    event.getShortCode(), e
            );
        }
    }
}