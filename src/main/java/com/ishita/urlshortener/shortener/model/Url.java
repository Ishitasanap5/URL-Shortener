package com.ishita.urlshortener.shortener.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "urls",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "shortCode"),
                @UniqueConstraint(columnNames = "longUrl")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Url {

    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String shortCode;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String longUrl;

    private Instant createdAt;
}