package com.ishita.urlshortener.shortener.service;

import com.ishita.urlshortener.shortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service

@RequiredArgsConstructor

public class ClickService {

    private final UrlRepository urlRepository;

    public void incrementClick(String shortCode){

        urlRepository.incrementClickCount(shortCode);

    }

}