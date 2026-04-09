package com.neversion.api.reservation.infrastructure.adapters.out;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reservation_details")
@Getter
@Setter
@Builder
public class ReservationDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", insertable = false, updatable = false)
    private BigDecimal subtotal;

    public ReservationDetailEntity() {
    }

    public ReservationDetailEntity(UUID id, UUID reservationId, Long inventoryId,
            Integer qty, BigDecimal unitPrice, BigDecimal subtotal) {
        this.id = id;
        this.reservationId = reservationId;
        this.inventoryId = inventoryId;
        this.qty = qty;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }
}
