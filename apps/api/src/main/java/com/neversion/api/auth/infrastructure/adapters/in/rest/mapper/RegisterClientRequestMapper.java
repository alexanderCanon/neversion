package com.neversion.api.auth.infrastructure.adapters.in.rest.mapper;

import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.RegisterClientRequest;
import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.RegisterClientResponse;
import com.neversion.api.user.domain.model.RegisterClientCommand;
import com.neversion.api.user.domain.model.RegisterClientResult;

/**
 * Stateless mapper between REST DTOs and domain objects for client registration.
 * Manual mapping — no MapStruct per project conventions.
 */
public final class RegisterClientRequestMapper {

    private RegisterClientRequestMapper() {
        // Utility class — not instantiable
    }

    /**
     * Maps the incoming REST request to a domain command.
     */
    public static RegisterClientCommand toCommand(RegisterClientRequest request) {
        return new RegisterClientCommand(
                request.email(),
                request.password(),
                request.name(),
                request.phone(),
                request.vendorUuid(),
                request.externalId());
    }

    /**
     * Maps the domain result to the REST response DTO.
     */
    public static RegisterClientResponse toResponse(RegisterClientResult result) {
        return new RegisterClientResponse(
                result.externalId(),
                result.clientUuid(),
                result.name(),
                result.email());
    }

}
