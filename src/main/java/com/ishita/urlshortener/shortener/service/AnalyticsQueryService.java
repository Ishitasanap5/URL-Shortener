package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.shortener.model.ClickEvent;
import com.ishita.urlshortener.shortener.repository.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AnalyticsQueryService {

    private final ClickEventRepository clickEventRepository;

    public void recordClick(String shortCode,
                            String ipAddress,
                            String userAgent,
                            String referrer) {

        ClickEvent event = ClickEvent.builder()
                .shortCode(shortCode)
                .clickedAt(Instant.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .referrer(referrer)
                .build();

        clickEventRepository.save(event);
    }
}