package com.neversion.api.profile.infrastructure.adapters.in.rest.mapper;

import org.springframework.stereotype.Component;

import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.infrastructure.adapters.in.rest.dto.ProfileRequest;
import com.neversion.api.profile.infrastructure.adapters.in.rest.dto.ProfileResponse;

@Component
public class ProfileMapper {

    public Profile toDomain(ProfileRequest request) {
        return request != null ? Profile.builder()
                .accountUuid(request.accountId())   // UUID — resolved to Long by service layer
                .name(request.name())
                .pin(request.pin())
                .notes(request.notes())
                .isOwner(request.isOwner() != null ? request.isOwner() : false)
                .build() : null;
    }

    public ProfileResponse toResponse(Profile profile) {
        return profile != null ? ProfileResponse.builder()
                .id(profile.getUuid())
                .accountId(profile.getAccountId())
                .name(profile.getName())
                .pin(profile.getPin())
                .notes(profile.getNotes())
                .isOwner(profile.getIsOwner())
                .status(profile.getStatus())
                .createdAt(profile.getCreatedAt())
                .build() : null;
    }
}
