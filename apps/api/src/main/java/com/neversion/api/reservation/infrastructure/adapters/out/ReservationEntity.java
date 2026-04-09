package com.neversion.api.reservation.infrastructure.adapters.out;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.neversion.api.reservation.domain.model.enums.ReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@Builder
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "discount")
    private BigDecimal discount;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "receipt_url", unique = true)
    private String receiptUrl;

    @Column(name = "expiration_date", nullable = false)
    private OffsetDateTime expirationDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public ReservationEntity() {
    }

    public ReservationEntity(UUID id, Long clientId, BigDecimal discount, BigDecimal total,
            ReservationStatus status, String receiptUrl,
            OffsetDateTime expirationDate, OffsetDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.discount = discount;
        this.total = total;
        this.status = status;
        this.receiptUrl = receiptUrl;
        this.expirationDate = expirationDate;
        this.createdAt = createdAt;
    }
}
