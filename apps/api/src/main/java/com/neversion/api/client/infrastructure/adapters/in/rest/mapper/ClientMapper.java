package com.neversion.api.client.infrastructure.adapters.in.rest.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.client.domain.model.Client;
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
}
