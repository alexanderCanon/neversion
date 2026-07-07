package com.neversion.api.gamesku.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for a GameSku (e.g. "Free Fire 110 Diamonds").
 * A GameSku belongs to a parent Game via the gameId field.
 *
 * See ADR-11: the parent-child relationship with Game is modeled via a
 * plain FK column (game_skus.game_id), not a JPA @ManyToOne. The
 * gameUuid field is transient and used only to carry the UUID from the
 * REST request; it is resolved to gameId in the application service.
 */
@Getter
@Setter
@Builder
public class GameSku {

    private Long id;
    private UUID uuid;
    private Long vendorId;
    private Long gameId;
    private UUID gameUuid;
    private String code;
    private String name;
    private BigDecimal price;
    private String imageUrl;

    @lombok.Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;

    public GameSku() {
    }

    public GameSku(Long id, UUID uuid, Long vendorId, Long gameId, UUID gameUuid,
                   String code, String name, BigDecimal price, String imageUrl,
                   Boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.vendorId = vendorId;
        this.gameId = gameId;
        this.gameUuid = gameUuid;
        this.code = code;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
}
