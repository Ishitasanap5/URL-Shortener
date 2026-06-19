package com.ishita.urlshortener.shortener.repository;

import com.ishita.urlshortener.shortener.model.Url;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);

    Optional<Url> findByLongUrl(String longUrl);

    @Modifying
    @Transactional
    @Query("""
           UPDATE Url u
           SET u.clickCount = u.clickCount + 1
           WHERE u.shortCode = :shortCode
           """)
    void incrementClickCount(
            @Param("shortCode") String shortCode
    );
}