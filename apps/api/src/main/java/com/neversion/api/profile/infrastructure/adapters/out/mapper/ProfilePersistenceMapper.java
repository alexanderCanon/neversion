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
                .notes(entity.getNotes())
                .isOwner(entity.getIsOwner())
                .status(entity.getStatus())
                .vendorId(entity.getVendorId())
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
                .notes(domain.getNotes())
                .isOwner(domain.getIsOwner())
                .status(domain.getStatus())
                .vendorId(domain.getVendorId())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
