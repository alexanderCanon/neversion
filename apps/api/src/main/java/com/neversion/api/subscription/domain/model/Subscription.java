package com.neversion.api.subscription.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.neversion.api.account.domain.model.enums.SaleMode;
import com.neversion.api.shared.domain.model.enums.AccountPreference;
import com.neversion.api.subscription.domain.model.enums.SubStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for an active subscription.
 * Represents the binding link between a Client and a specific Profile,
 * along with their payment timeline (start_date → payment_due_date).
 *
 * 'id' (Long)  – internal identifier, used only for DB relations. Never exposed externally.
 * 'uuid' (UUID) – external identifier exposed in all REST responses and frontend routes.
 * 'paymentDueDate' is the critical field used to track when payment is due.
 *
 * UUID transient fields (profileUuid, clientUuid, accountUuid) are populated from the REST
 * request and resolved to Long IDs inside the application service before persistence.
 */
@Getter
@Setter
@Builder
public class Subscription {

    /** Internal DB PK — used for JPA relations. */
    private Long id;

    /** External identifier — exposed to the frontend instead of the numeric id. */
    private UUID uuid;

    // ── Internal FK IDs (resolved before persistence) ───────────────────────

    /** FK to Client (Long) — resolved from clientUuid in service layer. */
    private Long clientId;

    /** FK to Profile (Long) — resolved from profileUuid in service layer. */
    private Long profileId;

    /** FK to Order (Long) — null for manual assignments outside storefront flow. */
    private Long orderId;

    /** FK to Service (Long) — financial snapshot for reporting and detail views. */
    private Long serviceId;

    // ── Transient UUID fields — sent by the REST layer, resolved in service ──

    /** Incoming UUID from the REST request for the target Profile. */
    private UUID profileUuid;

    /** Incoming UUID from the REST request for the Client. */
    private UUID clientUuid;

    /** Incoming UUID from the REST request for the Account (for context/display). */
    private UUID accountUuid;

    /** Incoming UUID from the REST request for the Service. */
    private UUID serviceUuid;

    // ── Business fields ──────────────────────────────────────────────────────

    /** Date the client's access lifecycle began. Defaults to today. */
    private LocalDate startDate;

    /** Date when the assigned access expires. */
    private LocalDate endDate;

    /**
     * The date by which the client must pay to retain access.
     */
    private LocalDate paymentDueDate;

    /** Number of months the client has paid. Incremented on each renewal. */
    private Long monthsPaid;

    /** Commercial price agreed for this subscription at creation time. */
    private BigDecimal priceSold;

    /** Discount applied at creation time, if any. */
    private BigDecimal discountApplied;

    /** Sale mode snapshot at creation time. */
    private SaleMode saleMode;

    /** Client's delivery preference for PERSONAL_ACCOUNT services. Null otherwise. */
    private AccountPreference accountPreference;

    /**
     * Current access status.
     * ACTIVE    – client has valid access.
     * SUSPENDED – missed payment window; access cut, reactivation possible.
     * CANCELLED – permanent termination (BR-11).
     */
    private SubStatus status;

    /** Admin notes for this subscription (e.g. "has 35 credit"). */
    private String notes;

    /** FK to vendors.id — multi-tenancy isolation (ADR-02, US-007). */
    private Long vendorId;

    private LocalDateTime createdAt;

    public Subscription() {
    }

    public Subscription(Long id, UUID uuid, Long clientId, Long profileId, Long orderId, Long serviceId,
            UUID profileUuid, UUID clientUuid, UUID accountUuid, UUID serviceUuid,
            LocalDate startDate, LocalDate endDate, LocalDate paymentDueDate,
            Long monthsPaid, BigDecimal priceSold, BigDecimal discountApplied, SaleMode saleMode,
            AccountPreference accountPreference, SubStatus status, String notes, Long vendorId,
            LocalDateTime createdAt) {
        this.id = id;
        this.uuid = uuid;
        this.clientId = clientId;
        this.profileId = profileId;
        this.orderId = orderId;
        this.serviceId = serviceId;
        this.profileUuid = profileUuid;
        this.clientUuid = clientUuid;
        this.accountUuid = accountUuid;
        this.serviceUuid = serviceUuid;
        this.startDate = startDate;
        this.endDate = endDate;
        this.paymentDueDate = paymentDueDate;
        this.monthsPaid = monthsPaid;
        this.priceSold = priceSold;
        this.discountApplied = discountApplied;
        this.saleMode = saleMode;
        this.accountPreference = accountPreference;
        this.status = status;
        this.notes = notes;
        this.vendorId = vendorId;
        this.createdAt = createdAt;
    }
}
