package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.shortener.model.ClickEvent;
import com.ishita.urlshortener.shortener.repository.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ClickEventRepository clickEventRepository;

    @KafkaListener(topics = "click-events", groupId = "analytics-group")
    public void consume(String message) {

        String[] parts = message.split("\\|");

        ClickEvent event = ClickEvent.builder()
                .shortCode(parts[0])
                .ipAddress(parts[1])
                .userAgent(parts[2])
                .referrer(parts[3])
                .clickedAt(Instant.now())
                .build();

        clickEventRepository.save(event);
    }
}