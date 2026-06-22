package com.ishita.urlshortener.shortener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ishita.urlshortener.shortener.dto.ClickEventMessage;
import com.ishita.urlshortener.shortener.model.ClickEvent;
import com.ishita.urlshortener.shortener.repository.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final ClickEventRepository clickEventRepository;

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "click-events",
            groupId = "analytics-group"
    )
    public void consume(String message) {

        try {

            ClickEventMessage e =
                    objectMapper.readValue(
                            message,
                            ClickEventMessage.class
                    );

            ClickEvent event = ClickEvent.builder()
                    .shortCode(e.getShortCode())
                    .clickedAt(Instant.now())
                    .ipAddress(e.getIpAddress())
                    .userAgent(e.getUserAgent())
                    .referrer(e.getReferer())
                    .build();

            clickEventRepository.save(event);

            log.info(
                    "Saved click event for shortCode: {}",
                    e.getShortCode()
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to process Kafka message: {}",
                    message,
                    ex
            );
        }
    }
}