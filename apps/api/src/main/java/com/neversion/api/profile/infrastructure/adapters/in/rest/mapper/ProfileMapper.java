package com.neversion.api.profile.infrastructure.adapters.in.rest.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.infrastructure.adapters.in.rest.dto.ProfileRequest;
import com.neversion.api.profile.infrastructure.adapters.in.rest.dto.ProfileResponse;

@Component
public class ProfileMapper {

    public Profile toDomain(ProfileRequest request) {
        return request != null ? Profile.builder()
                .accountId(request.accountId())
                .name(request.name())
                .pin(request.pin())
                .isOwner(request.isOwner() != null ? request.isOwner() : false)
                .build() : null;
    }

    public ProfileResponse toResponse(Profile profile) {
        return profile != null ? ProfileResponse.builder()
                .id(profile.getUuid())
                .accountId(profile.getAccountId())
                .name(profile.getName())
                .pin(profile.getPin())
                .isOwner(profile.getIsOwner())
                .createdAt(profile.getCreatedAt())
                .build() : null;
    }
}
