package com.neversion.api.reservation.infrastructure.adapters.out;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.neversion.api.reservation.domain.model.enums.ReservationStatus;
import com.neversion.api.reservation.infrastructure.adapters.out.converter.ReservationStatusConverter;
import com.neversion.api.shared.domain.model.enums.AccountPreference;
import com.neversion.api.shared.domain.model.enums.AccountPreferenceConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity for 'reservations' table.
 * US-009: PK BIGINT IDENTITY + uuid column + vendor_id.
 */
@Entity
@Table(name = "reservations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "uuid", updatable = false, nullable = false,
            columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Convert(converter = ReservationStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    /** Client's delivery preference for PERSONAL_ACCOUNT services. Null otherwise. */
    @Convert(converter = AccountPreferenceConverter.class)
    @Column(name = "account_preference", length = 20)
    private AccountPreference accountPreference;

    @Column(name = "receipt_url", unique = true)
    private String receiptUrl;

    /** Payment method selected by the client at checkout (EPIC-05, BR-06). */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "expiration_date")
    private OffsetDateTime expirationDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "renewal_subscription_id")
    private Long renewalSubscriptionId;

    @Column(name = "points_redeemed", nullable = false)
    @Builder.Default
    private Long pointsRedeemed = 0L;

    @Column(name = "points_discount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal pointsDiscount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
