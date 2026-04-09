package com.neversion.api.client.infrastructure.adapters.in.rest.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientRequest;
import com.neversion.api.client.infrastructure.adapters.in.rest.dto.ClientResponse;

@Component
public class ClientMapper {

    public Client toDomain(ClientRequest request) {
        return request != null ? Client.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .notes(request.notes())
                .build() : null;
    }

    public ClientResponse toResponse(Client client) {
        return client != null ? ClientResponse.builder()
                .id(client.getUuid())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .notes(client.getNotes())
                .createdAt(client.getCreatedAt())
                .build() : null;
    }
}
