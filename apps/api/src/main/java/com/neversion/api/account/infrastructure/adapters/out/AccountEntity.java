package com.neversion.api.account.infrastructure.adapters.out;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.account.domain.model.enums.SaleMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity for the 'accounts' table.
 * Represents a master credential purchased from a wholesale provider
 * to operate a specific Service (e.g., a Netflix family account).
 *
 * 'id' (Long) is the internal PK used for DB relations.
 * 'uuid' (UUID) is the external identifier exposed to the frontend.
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    /** Internal auto-increment PK — never exposed to the frontend. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** External UUID — used in all REST responses and frontend routes. */
    @Column(name = "uuid", updatable = false, nullable = false,
            columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    /** FK to services.id — the platform this account belongs to. */
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    /** Provider master email credential. */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /** Provider master password credential. */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * The date Neversion must pay the wholesaler to keep this account alive.
     * Polled by background automations (n8n).
     */
    @Column(name = "renewal_date", nullable = false)
    private LocalDate renewalDate;

    /** Quality tier, e.g. "4K Ultra HD", "Familiar". */
    @Column(name = "plan", length = 100)
    private String plan;

    /**
     * Determines sales strategy: by individual profiles or as a full account.
     * Values: BY_PROFILE | FULL_ACCOUNT (stored as VARCHAR in DB).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sale_mode", nullable = false, length = 20)
    private SaleMode saleMode;

    /** Private admin notes about this account. */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
