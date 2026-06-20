package com.ishita.urlshortener.shortener.repository;

import com.ishita.urlshortener.shortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    List<ClickEvent> findByShortCode(String shortCode);

    @Query("""
        SELECT COUNT(c)
        FROM ClickEvent c
        WHERE c.shortCode = :shortCode
    """)
    long totalClicks(@Param("shortCode") String shortCode);
}