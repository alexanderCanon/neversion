package com.neversion.api.order.infrastructure.adapters.out;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.neversion.api.order.domain.model.enums.OrderStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * JPA Entity for the 'orders' table.
 * US-008: PK migrated to BIGINT IDENTITY. UUID is now a separate column.
 * EPIC-05: Added client_id, payment_method, approved_at.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "uuid", updatable = false, nullable = false,
            columnDefinition = "uuid DEFAULT gen_random_uuid()")
    private UUID uuid;

    @Column(name = "reservation_id")
    private Long reservationId;

    /** FK to clients.id — the client who placed the order (EPIC-05). */
    @Column(name = "client_id")
    private Long clientId;

    /** FK to vendors.id — multi-tenancy (ADR-02, US-008). DB FK by V14. */
    @Column(name = "vendor_id")
    private Long vendorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /** Payment method provided by the client at checkout (BR-06). */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Timestamp when the vendor approved the receipt (US-035). */
    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (uuid == null) uuid = UUID.randomUUID();
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}
