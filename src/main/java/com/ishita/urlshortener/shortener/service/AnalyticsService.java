package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.shortener.dto.*;
import com.ishita.urlshortener.shortener.model.ClickEvent;
import com.ishita.urlshortener.shortener.repository.ClickEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService{

    private final ClickEventRepository clickEventRepository;

    public AnalyticsResponse getAnalytics(String shortCode) {

        List<ClickEvent> events =
                clickEventRepository.findByShortCode(shortCode);

        long totalClicks = events.size();

        // 📅 Daily clicks
        Map<LocalDate, Long> dailyMap = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getClickedAt()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate(),
                        Collectors.counting()
                ));

        List<DailyClickDto> dailyClicks = dailyMap.entrySet().stream()
                .map(e -> new DailyClickDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DailyClickDto::date))
                .toList();

        // 🌐 Referrer stats
        Map<String, Long> refMap = events.stream()
                .collect(Collectors.groupingBy(
                        e -> Optional.ofNullable(e.getReferrer())
                                .orElse("direct"),
                        Collectors.counting()
                ));

        List<ReferrerDto> referrers = refMap.entrySet().stream()
                .map(e -> new ReferrerDto(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.clicks(), a.clicks()))
                .toList();

        return new AnalyticsResponse(
                shortCode,
                totalClicks,
                dailyClicks,
                referrers
        );
    }
}