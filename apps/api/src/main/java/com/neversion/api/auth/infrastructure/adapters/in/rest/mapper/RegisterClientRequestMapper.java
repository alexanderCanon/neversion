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
                request.name(),
                request.phone(),
                request.vendorUuid());
    }

    /**
     * Maps the domain result to the REST response DTO.
     */
    public static RegisterClientResponse toResponse(RegisterClientResult result) {
        return new RegisterClientResponse(
                result.userUuid(),
                result.clientUuid(),
                result.name(),
                result.email(),
                result.temporaryPassword(),
                "MANUAL STEP REQUIRED: Create the Supabase Auth account using " +
                "email='" + result.email() + "' and the temporaryPassword above. " +
                "Then update users.external_id in the database with the Supabase user UUID.");
    }
}
