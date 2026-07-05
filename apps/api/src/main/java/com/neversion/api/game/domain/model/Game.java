package com.neversion.api.game.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for a game catalog item.
 */
@Getter
@Setter
@Builder
public class Game {

    private Long id;
    private UUID uuid;
    private Long vendorId;
    private String code;
    private String name;
    private BigDecimal price;
    private String imageUrl;

    @lombok.Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;

    public Game() {
    }

    public Game(Long id, UUID uuid, Long vendorId, String code, String name,
                BigDecimal price, String imageUrl, Boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.vendorId = vendorId;
        this.code = code;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
}
