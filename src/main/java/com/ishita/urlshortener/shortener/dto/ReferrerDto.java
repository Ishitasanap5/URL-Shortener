package com.ishita.urlshortener.shortener.dto;

public record ReferrerDto(
        String referrer,
        long clicks
) {}