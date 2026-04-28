package com.neversion.api.profile.infrastructure.adapters.in.rest.dto;

import com.neversion.api.profile.domain.model.enums.ProfileStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * Request body for US-027: manual profile status change.
 * Only AVAILABLE and BLOCKED are accepted; others return 400.
 */
@Builder
public record ChangeProfileStatusRequest(
        @NotNull(message = "Status is required") ProfileStatus status) {
}
