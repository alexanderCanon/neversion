package com.neversion.api.client.infrastructure.adapters.in.rest.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientAccessDetail;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientDetail;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientOrderHistoryDetail;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientOrderServiceDetail;
import com.neversion.api.client.application.port.in.ClientUseCase.ClientReservationStatusDetail;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientAccessResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientDetailResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientOrderHistoryResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientOrderServiceResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientReservationStatusResponse;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientRequest;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientResponse;

@Component
public class ClientMapper {

    /** Maps a creation request to a domain Client (no vendorId — resolved in service). */
    public Client toDomain(ClientRequest request) {
        if (request == null) return null;
        return Client.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .notes(request.notes())
                .build();
    }

    /**
     * Maps domain Client to ClientResponse.
     * US-029: activeSubscriptionCount pre-calculated by the service layer.
     */
    public ClientResponse toResponse(Client client, long activeSubscriptionCount) {
        if (client == null) return null;
        return ClientResponse.builder()
                .id(client.getUuid())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .notes(client.getNotes())
                .activeSubscriptionCount(activeSubscriptionCount)
                .createdAt(client.getCreatedAt())
                .build();
    }

    /**
     * Convenience overload — activeSubscriptionCount = 0.
     * Used for single-client operations (create, update, getById) where the count is not loaded.
     */
    public ClientResponse toResponse(Client client) {
        return toResponse(client, 0L);
    }

    /** Maps domain ClientDetail to REST ClientDetailResponse (US-030). */
    public ClientDetailResponse toDetailResponse(ClientDetail detail) {
        if (detail == null) return null;
        return new ClientDetailResponse(
                toResponse(detail.client()),
                detail.activeSubscriptions() == null ? java.util.List.of()
                        : detail.activeSubscriptions().stream()
                                .map(s -> new ClientDetailResponse.ActiveSubscriptionSummaryDto(
                                        s.id(), s.serviceName(), s.profileName(),
                                        s.paymentDueDate(), s.status()))
                                .toList(),
                detail.orderHistory() == null ? java.util.List.of()
                        : detail.orderHistory().stream()
                                .map(o -> new ClientDetailResponse.OrderSummaryDto(
                                        o.id(), o.status(), o.createdAt()))
                                .toList());
    }

    /** Maps domain access details to REST ClientAccessResponse (US-041). */
    public ClientAccessResponse toAccessResponse(ClientAccessDetail detail) {
        if (detail == null) return null;
        return ClientAccessResponse.builder()
                .subscriptionId(detail.subscriptionId())
                .serviceName(detail.serviceName())
                .accountEmail(detail.accountEmail())
                .accountPassword(detail.accountPassword())
                .profileName(detail.profileName())
                .profilePin(detail.profilePin())
                .paymentDueDate(detail.paymentDueDate())
                .status(detail.status())
                .build();
    }

    /** Maps client-owned order history details to REST responses (EPIC-09 / US-059). */
    public ClientOrderHistoryResponse toOrderHistoryResponse(ClientOrderHistoryDetail detail) {
        if (detail == null) return null;
        return new ClientOrderHistoryResponse(
                detail.id(),
                detail.reservationId(),
                detail.status(),
                detail.paymentMethod(),
                detail.total(),
                detail.discount(),
                detail.receiptUrl(),
                detail.approvedAt(),
                detail.createdAt(),
                detail.services() == null
                        ? java.util.List.of()
                        : detail.services().stream()
                                .map(this::toOrderServiceResponse)
                                .toList());
    }

    /** Maps reservation/receipt statuses for the client panel (EPIC-09 / US-060). */
    public ClientReservationStatusResponse toReservationStatusResponse(ClientReservationStatusDetail detail) {
        if (detail == null) return null;
        return new ClientReservationStatusResponse(
                detail.id(),
                detail.status(),
                detail.total(),
                detail.discount(),
                detail.receiptUrl(),
                detail.paymentMethod(),
                detail.expirationDate(),
                detail.createdAt(),
                detail.notes(),
                detail.renewalSubscriptionId(),
                detail.services() == null
                        ? java.util.List.of()
                        : detail.services().stream()
                                .map(this::toOrderServiceResponse)
                                .toList());
    }

    private ClientOrderServiceResponse toOrderServiceResponse(ClientOrderServiceDetail detail) {
        return new ClientOrderServiceResponse(
                detail.serviceId(),
                detail.serviceName(),
                detail.quantity());
    }
}
