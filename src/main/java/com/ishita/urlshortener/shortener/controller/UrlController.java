package com.ishita.urlshortener.shortener.controller;

import com.ishita.urlshortener.shortener.dto.ClickEventMessage;
import com.ishita.urlshortener.shortener.dto.ShortenRequest;
import com.ishita.urlshortener.shortener.dto.ShortenResponse;
import com.ishita.urlshortener.shortener.service.KafkaProducerService;
import com.ishita.urlshortener.shortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlShortenerService service;
    private final KafkaProducerService kafkaProducerService;
    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shortenUrl(
            @RequestBody @Valid ShortenRequest request
    ) {
        return ResponseEntity.ok(
                service.shortenUrl(request.longUrl())
        );
    }

    @GetMapping("/{shortCode}")
    public void redirect(
            @PathVariable String shortCode,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        String originalUrl = service.getOriginalUrl(shortCode);

        ClickEventMessage event = new ClickEventMessage(
                shortCode,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("Referer")
        );

        kafkaProducerService.sendClickEvent(event);


        response.sendRedirect(originalUrl);
    }
}