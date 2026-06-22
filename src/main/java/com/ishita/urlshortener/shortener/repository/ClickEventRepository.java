package com.ishita.urlshortener.shortener.repository;

import com.ishita.urlshortener.shortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    @Query("""
        SELECT COUNT(c)
        FROM ClickEvent c
        WHERE c.shortCode = :shortCode
    """)
    Long totalClicks(@Param("shortCode") String shortCode);

    @Query("""
        SELECT FUNCTION('DATE', c.clickedAt), COUNT(c)
        FROM ClickEvent c
        WHERE c.shortCode = :shortCode
        GROUP BY FUNCTION('DATE', c.clickedAt)
        ORDER BY FUNCTION('DATE', c.clickedAt)
    """)
    List<Object[]> findDailyClicks(@Param("shortCode") String shortCode);

    @Query("""
        SELECT c.referrer, COUNT(c)
        FROM ClickEvent c
        WHERE c.shortCode = :shortCode
        GROUP BY c.referrer
        ORDER BY COUNT(c) DESC
    """)
    List<Object[]> findReferrerStats(@Param("shortCode") String shortCode);
}