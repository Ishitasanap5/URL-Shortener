package com.ishita.urlshortener.shortener.repository;

import com.ishita.urlshortener.shortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    @Query("SELECT COUNT(c) FROM ClickEvent c WHERE c.shortCode = :shortCode")
    Long totalClicks(@Param("shortCode") String shortCode);

    @Query(
            value = "SELECT DATE(clicked_at) as date, COUNT(*) as count " +
                    "FROM click_events WHERE short_code = :shortCode " +
                    "GROUP BY DATE(clicked_at) ORDER BY DATE(clicked_at)",
            nativeQuery = true
    )
    List<Object[]> findDailyClicks(@Param("shortCode") String shortCode);

    @Query(
            value = "SELECT referrer, COUNT(*) as count " +
                    "FROM click_events WHERE short_code = :shortCode " +
                    "GROUP BY referrer ORDER BY count DESC",
            nativeQuery = true
    )
    List<Object[]> findReferrerStats(@Param("shortCode") String shortCode);
}