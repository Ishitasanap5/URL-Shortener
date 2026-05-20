package com.ishita.urlshortener.shortener.controller;

import com.ishita.urlshortener.shortener.dto.ShortenRequest;
import com.ishita.urlshortener.shortener.dto.ShortenResponse;
import com.ishita.urlshortener.shortener.service.UrlShortenerService;
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
            HttpServletResponse response
    ) throws IOException {

        String originalUrl = service.getOriginalUrl(shortCode);

        response.sendRedirect(originalUrl);
    }
}