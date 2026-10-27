package com.ishita.urlshortener.shortener.model;

import jakarta.persistence.*;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Entity
@Table(
        name = "urls",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "shortCode"),
                @UniqueConstraint(columnNames = "longUrlHash")
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

    @Column(nullable = false, length = 2048)
    private String longUrl;

    @Column(nullable = false, length = 64)
    private String longUrlHash;

    private Instant createdAt;

    @PrePersist
    @PreUpdate
    void computeHash() {
        this.longUrlHash = sha256(this.longUrl);
    }

    public static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}