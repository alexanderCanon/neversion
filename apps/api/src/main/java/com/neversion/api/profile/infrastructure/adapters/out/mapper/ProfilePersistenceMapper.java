package com.neversion.api.profile.infrastructure.adapters.out.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.infrastructure.adapters.out.ProfileEntity;

@Component
public class ProfilePersistenceMapper {

    public Profile toDomain(ProfileEntity entity) {
        if (entity == null) return null;
        return Profile.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .accountId(entity.getAccountId())
                .name(entity.getName())
                .pin(entity.getPin())
                .isOwner(entity.getIsOwner())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ProfileEntity toEntity(Profile domain) {
        if (domain == null) return null;
        return ProfileEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .accountId(domain.getAccountId())
                .name(domain.getName())
                .pin(domain.getPin())
                .isOwner(domain.getIsOwner())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
