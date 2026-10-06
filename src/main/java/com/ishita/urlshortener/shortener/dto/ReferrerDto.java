package com.ishita.urlshortener.shortener.dto;

public record ReferrerDto(
        String referrer,
        Long count
) {}