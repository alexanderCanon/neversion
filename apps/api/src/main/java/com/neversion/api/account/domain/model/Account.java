package com.neversion.api.account.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.account.domain.model.enums.SaleMode;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain model for a master streaming account credential.
 * Purchased by Neversion from a wholesaler and associated to a specific Service.
 *
 * 'id' (Long)  – internal identifier, used only for DB relations. Never exposed externally.
 * 'uuid' (UUID) – external identifier exposed in all REST responses and frontend routes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
public class Account {

    /** Internal DB PK — used for JPA relations (profiles FK). */
    private Long id;

    /** External identifier — exposed to the frontend instead of the numeric id. */
    private UUID uuid;

    /** FK to Service — the platform this account belongs to. */
    private Long serviceId;

    /** Master email credential used to log into the streaming platform. */
    private String email;

    /** Master password credential for the streaming platform. */
    private String password;

    /**
     * The date Neversion must renew payment to the wholesaler.
     * Tracked by n8n automations to prevent service interruption.
     */
    private LocalDate renewalDate;

    /** Quality tier of this account (e.g. "Familiar", "4K Ultra HD"). */
    private String plan;

    /**
     * Sales strategy: sell individual profiles or the full account as one unit.
     * Values: BY_PROFILE | FULL_ACCOUNT
     */
    private SaleMode saleMode;

    /** Private admin-only notes for this account. */
    private String notes;

    private LocalDateTime createdAt;

    public Account(Long id, UUID uuid, Long serviceId, String email, String password,
            LocalDate renewalDate, String plan, SaleMode saleMode, String notes,
            LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.serviceId = serviceId;
        this.email = email;
        this.password = password;
        this.renewalDate = renewalDate;
        this.plan = plan;
        this.saleMode = saleMode;
        this.notes = notes;
        this.createdAt = createdAt;
    }
}
