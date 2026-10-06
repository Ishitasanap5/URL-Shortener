package com.ishita.urlshortener.shortener.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "click_events",
        indexes = {
                @Index(name = "idx_click_short_code", columnList = "short_code")
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
    private Long id;

    @Column(nullable = false, name = "short_code")
    private String shortCode;

    @Column(name = "clicked_at")
    private Instant clickedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    private String referrer;
}