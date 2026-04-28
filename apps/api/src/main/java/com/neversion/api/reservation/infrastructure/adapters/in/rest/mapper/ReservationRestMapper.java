package com.neversion.api.reservation.infrastructure.adapters.in.rest.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.neversion.api.reservation.application.port.in.ReservationItemCommand;
import com.neversion.api.reservation.domain.model.Reservation;
import com.neversion.api.reservation.domain.model.ReservationDetail;
import com.neversion.api.reservation.infrastructure.adapters.in.rest.dto.ReservationDetailResponse;
import com.neversion.api.reservation.infrastructure.adapters.in.rest.dto.ReservationItemRequest;
import com.neversion.api.reservation.infrastructure.adapters.in.rest.dto.ReservationResponse;

@Component
public class ReservationRestMapper {

    public List<ReservationItemCommand> toItemCommands(List<ReservationItemRequest> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(item -> new ReservationItemCommand(item.serviceId(), item.qty()))
                .collect(Collectors.toList());
    }

    public ReservationResponse toResponse(Reservation reservation) {
        List<ReservationDetailResponse> detailResponses = reservation.getDetails() == null
                ? Collections.emptyList()
                : reservation.getDetails().stream()
                        .map(this::toDetailResponse)
                        .collect(Collectors.toList());

        return new ReservationResponse(
                reservation.getUuid(),
                reservation.getClientUuid(),
                reservation.getStatus(),
                reservation.getDiscount(),
                reservation.getTotal(),
                reservation.getReceiptUrl(),
                reservation.getPaymentMethod(),
                reservation.getExpirationDate(),
                reservation.getCreatedAt(),
                detailResponses);
    }

    private ReservationDetailResponse toDetailResponse(ReservationDetail detail) {
        return new ReservationDetailResponse(
                detail.uuid(),
                detail.serviceId(),
                detail.qty(),
                detail.unitPrice(),
                detail.subtotal());
    }
}
