package com.neversion.api.account.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.account.domain.model.enums.ProfileDeliveryType;
import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.shared.domain.model.enums.AccountStatus;

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

    /**
     * Transient — carries the service UUID from the REST request to the service layer.
     * Resolved to serviceId (Long) by CreateAccountService / UpdateAccountService.
     * Never persisted.
     */
    private UUID serviceUuid;

    /** Master email credential used to log into the streaming platform. */
    private String email;

    /** Master password credential for the streaming platform. */
    private String password;

    /**
     * The date Neversion must renew payment to the wholesaler.
     */
    private LocalDate renewalDate;

    /** Quality tier of this account (e.g. "Familiar", "4K Ultra HD"). */
    private String plan;

    /**
     * Sales strategy: sell individual profiles or the full account as one unit.
     * Values: BY_PROFILE | FULL_ACCOUNT
     */
    private SaleMode saleMode;

    /** How access is delivered to the client for BY_PROFILE accounts. Null for FULL_ACCOUNT. */
    private ProfileDeliveryType profileDeliveryType;

    /** Private admin-only notes for this account. */
    private String notes;

    /** Acquisition cost paid to the wholesaler (US-006). */
    private java.math.BigDecimal cost;

    /** Where this account was purchased from (US-006). */
    private String source;

    /** Date the account was purchased from the wholesaler (US-006). */
    private LocalDate purchasedAt;

    /** Operational status: available | partial | full | expired (US-006). */
    @Builder.Default
    private AccountStatus status = AccountStatus.AVAILABLE;

    /** Maximum number of profiles this account can hold. Defaults from service template. */
    @Builder.Default
    private Integer maxProfiles = 1;

    /** FK to vendors.id — multi-tenancy (ADR-02, US-006). */
    private Long vendorId;

    private LocalDateTime createdAt;

    public Account(Long id, UUID uuid, Long serviceId, UUID serviceUuid, String email, String password,
            LocalDate renewalDate, String plan, SaleMode saleMode, ProfileDeliveryType profileDeliveryType,
            String notes, java.math.BigDecimal cost, String source, LocalDate purchasedAt,
            AccountStatus status, Integer maxProfiles, Long vendorId, LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.serviceId = serviceId;
        this.serviceUuid = serviceUuid;
        this.email = email;
        this.password = password;
        this.renewalDate = renewalDate;
        this.plan = plan;
        this.saleMode = saleMode;
        this.profileDeliveryType = profileDeliveryType;
        this.notes = notes;
        this.cost = cost;
        this.source = source;
        this.purchasedAt = purchasedAt;
        this.status = status;
        this.maxProfiles = maxProfiles != null ? maxProfiles : 1;
        this.vendorId = vendorId;
        this.createdAt = createdAt;
    }
}
