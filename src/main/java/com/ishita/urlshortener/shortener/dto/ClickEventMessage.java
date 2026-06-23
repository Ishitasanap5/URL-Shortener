package com.ishita.urlshortener.shortener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClickEventMessage {

    private String shortCode;
    private String ipAddress;
    private String userAgent;
    private String referrer;
    private Instant clickedAt;
}