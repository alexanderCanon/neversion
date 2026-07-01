package com.neversion.api.loyalty.application.port.in;

import java.util.UUID;

import com.neversion.api.loyalty.application.port.in.dto.PointsSummary;

/**
 * Retrieves a client's points summary (available / pending / total).
 */
public interface GetPointsSummaryUseCase {

    /** Resolves the summary for the authenticated client (Store, "me" endpoint). */
    PointsSummary getForAuthenticatedClient(String callerExternalId);

    /** Resolves the summary for a specific client, with vendor-ownership enforcement (Panel). */
    PointsSummary getForClientAsVendor(String callerExternalId, UUID clientUuid);
}
