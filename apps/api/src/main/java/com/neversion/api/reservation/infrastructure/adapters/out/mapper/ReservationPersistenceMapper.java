package com.neversion.api.reservation.infrastructure.adapters.out.mapper;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.infrastructure.adapters.out.ReservationDetailEntity;
import com.neversion.api.reservation.infrastructure.adapters.out.ReservationEntity;

/**
 * US-009/US-010: explicit mapper — Long PK, uuid, vendorId, serviceId.
 */
@Component
public class ReservationPersistenceMapper {

    public Reservation toDomain(ReservationEntity entity) {
        if (entity == null) return null;
        return Reservation.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .clientId(entity.getClientId())
                .vendorId(entity.getVendorId())
                .discount(entity.getDiscount())
                .total(entity.getTotal())
                .receiptUrl(entity.getReceiptUrl())
                .paymentMethod(entity.getPaymentMethod())
                .status(entity.getStatus())
                .accountPreference(entity.getAccountPreference())
                .expirationDate(entity.getExpirationDate() != null
                        ? entity.getExpirationDate().toInstant() : null)
                .createdAt(entity.getCreatedAt() != null
                        ? entity.getCreatedAt().toInstant() : null)
                .notes(entity.getNotes())
                .renewalSubscriptionId(entity.getRenewalSubscriptionId())
                .pointsRedeemed(entity.getPointsRedeemed())
                .pointsDiscount(entity.getPointsDiscount())
                .build();
    }

    public ReservationEntity toEntity(Reservation domain) {
        if (domain == null) return null;
        return ReservationEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .clientId(domain.getClientId())
                .vendorId(domain.getVendorId())
                .discount(domain.getDiscount())
                .total(domain.getTotal())
                .receiptUrl(domain.getReceiptUrl())
                .paymentMethod(domain.getPaymentMethod())
                .status(domain.getStatus())
                .accountPreference(domain.getAccountPreference())
                .expirationDate(domain.getExpirationDate() != null
                        ? domain.getExpirationDate().atOffset(ZoneOffset.UTC) : null)
                .notes(domain.getNotes())
                .renewalSubscriptionId(domain.getRenewalSubscriptionId())
                .pointsRedeemed(domain.getPointsRedeemed() != null ? domain.getPointsRedeemed() : 0L)
                .pointsDiscount(domain.getPointsDiscount() != null ? domain.getPointsDiscount() : java.math.BigDecimal.ZERO)
                .build();
    }

    public ReservationDetail toDomain(ReservationDetailEntity entity) {
        return new ReservationDetail(
                entity.getId(),
                entity.getUuid(),
                entity.getReservationId(),
                entity.getServiceId(),
                entity.getQty(),
                entity.getUnitPrice(),
                entity.getSubtotal());
    }

    public ReservationDetailEntity toEntity(ReservationDetail domain) {
        return ReservationDetailEntity.builder()
                .id(domain.id())
                .uuid(domain.uuid())
                .reservationId(domain.reservationId())
                .serviceId(domain.serviceId())
                .qty(domain.qty())
                .unitPrice(domain.unitPrice())
                .build();
    }
}
