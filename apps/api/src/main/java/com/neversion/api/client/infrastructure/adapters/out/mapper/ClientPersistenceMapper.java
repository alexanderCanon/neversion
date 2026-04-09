package com.neversion.api.client.infrastructure.adapters.out.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.infrastructure.adapters.out.ClientEntity;

@Component
public class ClientPersistenceMapper {

    public Client toDomain(ClientEntity entity) {
        if (entity == null) return null;
        return Client.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ClientEntity toEntity(Client domain) {
        if (domain == null) return null;
        return ClientEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .name(domain.getName())
                .phone(domain.getPhone())
                .email(domain.getEmail())
                .notes(domain.getNotes())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
