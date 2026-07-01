package com.neversion.api.loyalty.application.port.in;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.neversion.api.loyalty.application.port.in.dto.PointsMovementsPage;

/**
 * Lists a client's points ledger movements, paginated.
 */
public interface ListPointsMovementsUseCase {

    /** Movements for the authenticated client (Store, "me" endpoint). */
    PointsMovementsPage listForAuthenticatedClient(String callerExternalId, Pageable pageable);

    /** Movements for a specific client, with vendor-ownership enforcement (Panel). */
    PointsMovementsPage listForClientAsVendor(String callerExternalId, UUID clientUuid, Pageable pageable);
}
