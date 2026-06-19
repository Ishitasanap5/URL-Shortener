package com.ishita.urlshortener.shortener.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "click_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String shortCode;

    private Instant clickedAt;

    private String ipAddress;

    private String userAgent;

    private String referrer;

}
