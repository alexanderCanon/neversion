package com.neversion.api.reservation.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.shared.domain.model.enums.AccountPreference;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain model for a storefront reservation.
 * US-009: PK migrated from UUID to Long (BIGINT IDENTITY).
 * 'id' (Long) — internal PK. 'uuid' (UUID) — external identifier.
 */
@Getter
@Setter
@Builder
public class Reservation {

    private Long id;
    private UUID uuid;

    /** Internal DB PK of the Client — resolved from clientUuid before persistence. */
    private Long clientId;

    /** External identifier of the Client — received from the REST layer. */
    private UUID clientUuid;

    /** FK to vendors.id — multi-tenancy (ADR-02, US-009). */
    private Long vendorId;

    private BigDecimal discount;
    private BigDecimal total;
    private String receiptUrl;

    /** Payment method selected by the client at checkout (EPIC-05, BR-06). */
    private String paymentMethod;

    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    /** Client's delivery preference for PERSONAL_ACCOUNT services. Null otherwise. */
    private AccountPreference accountPreference;

    private Instant expirationDate;
    private Instant createdAt;
    private String notes;
    private Long renewalSubscriptionId;
    private UUID renewalSubscriptionUuid;
    private List<ReservationDetail> details;

    /** Points redeemed by the client as a discount at checkout (loyalty program). */
    @Builder.Default
    private Long pointsRedeemed = 0L;

    /** Discount amount in GTQ resulting from redeemed points (1 point = 1 GTQ). */
    @Builder.Default
    private BigDecimal pointsDiscount = BigDecimal.ZERO;

    public Reservation() {
    }

    public Reservation(Long id, UUID uuid, Long clientId, UUID clientUuid, Long vendorId,
            BigDecimal discount, BigDecimal total, String receiptUrl, String paymentMethod,
            ReservationStatus status, AccountPreference accountPreference, Instant expirationDate,
            Instant createdAt, String notes, Long renewalSubscriptionId, UUID renewalSubscriptionUuid,
            List<ReservationDetail> details, Long pointsRedeemed, BigDecimal pointsDiscount) {
        this.id = id;
        this.uuid = uuid;
        this.clientId = clientId;
        this.clientUuid = clientUuid;
        this.vendorId = vendorId;
        this.discount = discount;
        this.total = total;
        this.receiptUrl = receiptUrl;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.accountPreference = accountPreference;
        this.expirationDate = expirationDate;
        this.createdAt = createdAt;
        this.notes = notes;
        this.renewalSubscriptionId = renewalSubscriptionId;
        this.renewalSubscriptionUuid = renewalSubscriptionUuid;
        this.details = details;
        this.pointsRedeemed = pointsRedeemed;
        this.pointsDiscount = pointsDiscount;
    }
}
