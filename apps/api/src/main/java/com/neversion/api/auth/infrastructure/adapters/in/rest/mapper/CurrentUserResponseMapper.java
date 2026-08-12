package com.neversion.api.auth.infrastructure.adapters.in.rest.mapper;

import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.CurrentUserResponse;
import com.neversion.api.user.domain.model.CurrentUserContextResult;

import java.util.Locale;

public final class CurrentUserResponseMapper {

    private CurrentUserResponseMapper() {
    }

    public static CurrentUserResponse toResponse(CurrentUserContextResult result) {
        return new CurrentUserResponse(
                result.externalId(),
                result.role().name().toLowerCase(Locale.ROOT),
                result.vendorUuid(),
                result.storeName());
    }

}
