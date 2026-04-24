package com.neversion.api.user.application.port.in;

import com.neversion.api.user.domain.model.RegisterClientCommand;
import com.neversion.api.user.domain.model.RegisterClientResult;

/**
 * Inbound port — use case for client self-registration (US-013).
 * <p>
 * Implemented by RegisterClientService in the application layer.
 * This endpoint is public — any visitor to a vendor's store can register.
 */
public interface RegisterClientUseCase {

    /**
     * Registers a new client in the platform, linked to a specific vendor's store.
     * <p>
     * Orchestrates: vendor resolution → user persistence → client persistence → notification event.
     *
     * @param command all data required to create the client
     * @return result containing public identifiers and generated credentials
     */
    RegisterClientResult register(RegisterClientCommand command);
}
