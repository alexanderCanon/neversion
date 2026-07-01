package com.neversion.api.vendor.infrastructure.adapters.out;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the vendors table.
 * Infrastructure concern only — never crosses the domain boundary.
 * <p>
 * user_id is stored as a plain Long column. The FK constraint is enforced by the
 * database (V8 migration). No @ManyToOne to UserEntity — modules must not reference
 * each other's infrastructure internals (hexagonal architecture boundary).
 */
@Entity
@Table(name = "vendors")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    /**
     * FK to users.id — enforced at DB level by V8 migration.
     * Stored as plain Long; cross-module JPA relationships are forbidden
     * to preserve module boundary isolation.
     */
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "logo_url")
    private String logoUrl;

    /** JSONB column — bank/payment details (opaque to domain). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bank_details", columnDefinition = "jsonb")
    private String bankDetails;

    /** JSONB column — discount tier config (BR-13, opaque to domain). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "discount_cfg", columnDefinition = "jsonb")
    private String discountCfg;

    /** JSONB column — rewards/loyalty points config (opaque to domain). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rewards_cfg", columnDefinition = "jsonb")
    private String rewardsCfg;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
