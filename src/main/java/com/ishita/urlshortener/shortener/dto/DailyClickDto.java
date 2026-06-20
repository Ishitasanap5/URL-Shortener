package com.ishita.urlshortener.shortener.dto;

import java.time.LocalDate;

public record DailyClickDto(
        LocalDate date,
        long clicks
) {}