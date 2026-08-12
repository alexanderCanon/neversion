package com.neversion.api.user.infrastructure.adapters.out.mapper;

import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.infrastructure.adapters.out.UserEntity;

/**
 * Explicit mapper between User domain model and UserEntity.
 * No magic mapping — all fields mapped explicitly (GEMINI.md standards).
 */
public class UserPersistenceMapper {

    private UserPersistenceMapper() {}

    public static User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .externalId(entity.getExternalId())
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static UserEntity toEntity(User domain) {
        return UserEntity.builder()
                .id(domain.getId())
                .externalId(domain.getExternalId())
                .role(domain.getRole())
                .build();
    }
}

