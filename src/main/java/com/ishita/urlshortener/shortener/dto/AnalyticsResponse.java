package com.ishita.urlshortener.shortener.dto;

import java.util.List;

public record AnalyticsResponse(
        String shortCode,
        long totalClicks,
        List<DailyClickDto> dailyClicks,
        List<ReferrerDto> referrers
) {}