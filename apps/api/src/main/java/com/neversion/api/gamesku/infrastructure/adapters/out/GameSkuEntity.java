package com.neversion.api.gamesku.infrastructure.adapters.out;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for the 'game_skus' table (e.g. "Free Fire 110 Diamonds").
 * Belongs to a parent Game via the game_id FK column (ADR-11).
 *
 * 'id' (Long) is the internal PK used for DB relations.
 * 'uuid' (UUID) is the external identifier exposed to the frontend.
 */
@Entity
@Table(name = "game_skus")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSkuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "uuid", updatable = false, nullable = false,
            columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    /** FK to games.id — the parent game this SKU belongs to. Nullable for orphans. */
    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "code", nullable = false, length = 25)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
