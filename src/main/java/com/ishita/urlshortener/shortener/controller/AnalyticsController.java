package com.ishita.urlshortener.shortener.controller;

import com.ishita.urlshortener.shortener.dto.AnalyticsResponse;
import com.ishita.urlshortener.shortener.service.AnalyticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    @GetMapping("/{shortCode}")
    public AnalyticsResponse getAnalytics(@PathVariable String shortCode) {
        return analyticsQueryService.getAnalytics(shortCode);
    }
}