package com.ishita.urlshortener.shortener.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ishita.urlshortener.shortener.dto.ClickEventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, ClickEventMessage> kafkaTemplate;

    private static final String TOPIC = "click-events";

    public void sendClickEvent(ClickEventMessage event) {
        kafkaTemplate.send(TOPIC, event);
    }
}