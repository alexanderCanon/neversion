package com.neversion.api.auth.infrastructure.adapters.in.rest.mapper;

import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.RegisterVendorRequest;
import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.RegisterVendorResponse;
import com.neversion.api.user.domain.model.RegisterVendorCommand;
import com.neversion.api.user.domain.model.RegisterVendorResult;

/**
 * Stateless mapper between REST DTOs and domain objects for the auth module.
 * Manual mapping — no MapStruct per project conventions.
 */
public final class RegisterVendorRequestMapper {

    private RegisterVendorRequestMapper() {
        // Utility class — not instantiable
    }

    /**
     * Maps the incoming REST request to a domain command.
     *
     * @param request validated REST DTO
     * @return domain command
     */
    public static RegisterVendorCommand toCommand(RegisterVendorRequest request) {
        return new RegisterVendorCommand(
                request.email(),
                request.storeName(),
                request.logoUrl(),
                request.bankDetails(),
                request.discountCfg());
    }

    /**
     * Maps the domain result to the REST response DTO.
     * Appends the manual Supabase step reminder (ADR-09).
     *
     * @param result domain result from RegisterVendorService
     * @return REST response DTO
     */
    public static RegisterVendorResponse toResponse(RegisterVendorResult result) {
        return new RegisterVendorResponse(
                result.userUuid(),
                result.vendorUuid(),
                result.storeName(),
                result.email(),
                result.temporaryPassword(),
                "MANUAL STEP REQUIRED: Create the Supabase Auth account at " +
                "https://app.supabase.com → Authentication → Users → Invite User, " +
                "using email='" + result.email() + "' and the temporaryPassword above. " +
                "Then update users.external_id in the database with the Supabase user UUID.");
    }
}
