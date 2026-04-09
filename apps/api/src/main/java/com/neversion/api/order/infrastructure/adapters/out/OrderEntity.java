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
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "reservation_id", nullable = false, unique = true)
    private UUID reservationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public OrderEntity() {
    }

    public OrderEntity(UUID id, UUID reservationId, OrderStatus status,
            String notes, OffsetDateTime createdAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }
}
