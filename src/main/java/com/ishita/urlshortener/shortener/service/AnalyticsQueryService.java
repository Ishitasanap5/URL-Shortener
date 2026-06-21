package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.shortener.dto.AnalyticsResponse;
import com.ishita.urlshortener.shortener.model.ClickEvent;
import com.ishita.urlshortener.shortener.repository.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsQueryService {

    private final ClickEventRepository clickEventRepository;

    public AnalyticsResponse getAnalytics(String shortCode) {

        List<ClickEvent> events =
                clickEventRepository.findByShortCode(shortCode);

        long totalClicks = events.size();

        Map<String, Long> clicksByReferrer =
                events.stream()
                        .collect(Collectors.groupingBy(
                                e -> e.getReferrer() == null ? "direct" : e.getReferrer(),
                                Collectors.counting()
                        ));

        Map<String, Long> clicksByDay =
                events.stream()
                        .collect(Collectors.groupingBy(
                                e -> e.getClickedAt().toString().substring(0, 10),
                                Collectors.counting()
                        ));

        return new AnalyticsResponse(
                shortCode,
                totalClicks,
                clicksByReferrer,
                clicksByDay
        );
    }
}