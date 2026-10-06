package com.ishita.urlshortener.shortener.dto;

import java.util.List;

public record AnalyticsResponse(
        Long totalClicks,
        List<DailyClickDto> dailyClicks,
        List<ReferrerDto> referrerStats
) {}