package com.neversion.api.user.application.port.in;

import com.neversion.api.user.domain.model.RegisterVendorCommand;
import com.neversion.api.user.domain.model.RegisterVendorResult;

/**
 * Inbound port — use case for registering a new vendor in the platform.
 * <p>
 * Implemented by RegisterVendorService in the application layer.
 * Invoked exclusively by SUPER_ADMIN users (US-012 / ADR-08).
 */
public interface RegisterVendorUseCase {

    /**
     * Registers a new vendor user in the platform.
     * <p>
     * Orchestrates: user persistence → vendor persistence → notification event.
     *
     * @param command all data required to create the vendor
     * @return result containing public identifiers and generated credentials
     */
    RegisterVendorResult register(RegisterVendorCommand command);
}
