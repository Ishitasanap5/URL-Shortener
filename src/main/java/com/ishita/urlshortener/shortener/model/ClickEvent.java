package com.ishita.urlshortener.shortener.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "click_events",
        indexes = {
                @Index(name = "idx_click_short_code", columnList = "shortCode")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // FIXED: boxed Long instead of primitive long

    @Column(nullable = false)
    private String shortCode;

    private Instant clickedAt;

    private String ipAddress;

    private String userAgent;

    private String referrer;
}