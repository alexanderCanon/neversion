package com.neversion.api.profile.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ProfileRequest(
        @NotNull(message = "Account ID is required") Long accountId,

        @Size(max = 100, message = "Name must not exceed 100 characters") String name,

        @Size(max = 20, message = "Pin must not exceed 20 characters") String pin,

        Boolean isOwner) {
}
