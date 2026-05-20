package com.ishita.urlshortener.shortener.repository;

import com.ishita.urlshortener.shortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByLongUrl(String longUrl);
}