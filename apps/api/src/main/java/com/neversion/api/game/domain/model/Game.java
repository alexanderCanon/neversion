package com.neversion.api.game.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for a Game parent entity (e.g. "Free Fire", "Clash Royale").
 * A Game groups multiple GameSku children (e.g. "110 Diamonds", "220 Diamonds").
 *
 * See ADR-11: the parent-child relationship with GameSku is modeled via a
 * plain FK column (game_skus.game_id), not a JPA @ManyToOne.
 */
@Getter
@Setter
@Builder
public class Game {

    private Long id;
    private UUID uuid;
    private Long vendorId;
    private String name;
    private String slug;
    private String imageUrl;

    @lombok.Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;

    public Game() {
    }

    public Game(Long id, UUID uuid, Long vendorId, String name, String slug,
                String imageUrl, Boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.vendorId = vendorId;
        this.name = name;
        this.slug = slug;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
}
