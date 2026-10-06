package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.shortener.dto.AnalyticsResponse;
import com.ishita.urlshortener.shortener.dto.DailyClickDto;
import com.ishita.urlshortener.shortener.dto.ReferrerDto;
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

    private final ClickEventRepository repo;

    public AnalyticsResponse getAnalytics(String shortCode) {

        Long totalClicks = repo.totalClicks(shortCode);

        List<DailyClickDto> daily = repo.findDailyClicks(shortCode)
                .stream()
                .map(r -> new DailyClickDto(
                        r[0].toString(),
                        Long.parseLong(r[1].toString())
                ))
                .toList();

        List<ReferrerDto> referrers = repo.findReferrerStats(shortCode)
                .stream()
                .map(r -> new ReferrerDto(
                        r[0] == null ? "direct" : r[0].toString(),
                        Long.parseLong(r[1].toString())
                ))
                .toList();

        return new AnalyticsResponse(totalClicks, daily, referrers);
    }
}