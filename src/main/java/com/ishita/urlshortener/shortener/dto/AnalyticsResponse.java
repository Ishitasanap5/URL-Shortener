package com.ishita.urlshortener.shortener.dto;

import java.util.Map;

public record AnalyticsResponse(

        String shortCode,
        long totalClicks,
        Map<String, Long> clicksByReferrer,
        Map<String, Long> clicksByDay

) {
}