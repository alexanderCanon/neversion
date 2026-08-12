package com.neversion.api.auth.infrastructure.adapters.in.rest.mapper;

import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.RegisterVendorRequest;
import com.neversion.api.auth.infrastructure.adapters.in.rest.dto.RegisterVendorResponse;
import com.neversion.api.user.domain.model.RegisterVendorCommand;
import com.neversion.api.user.domain.model.RegisterVendorResult;

/**
 * Stateless mapper between REST DTOs and domain objects for the vendor auth module.
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
                request.password(),
                request.storeName(),
                request.logoUrl(),
                request.bankDetails(),
                request.discountCfg());
    }

    /**
     * Maps the domain result to the REST response DTO.
     *
     * @param result domain result from RegisterVendorService
     * @return REST response DTO
     */
    public static RegisterVendorResponse toResponse(RegisterVendorResult result) {
        return new RegisterVendorResponse(
                result.externalId(),
                result.vendorUuid(),
                result.storeName(),
                result.email());
    }

}
