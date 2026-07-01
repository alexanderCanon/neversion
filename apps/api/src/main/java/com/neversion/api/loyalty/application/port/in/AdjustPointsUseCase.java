package com.neversion.api.loyalty.application.port.in;

import java.util.UUID;

import com.neversion.api.loyalty.domain.model.PointsLedgerEntry;

/**
 * Manual points adjustment made by the vendor from the panel (+/-),
 * e.g. for refunds, promotions, or corrections.
 */
public interface AdjustPointsUseCase {

    /**
     * @param callerExternalId Supabase subject (JWT {@code sub}) of the authenticated vendor
     * @param clientUuid       external id of the client to adjust
     * @param points           amount to adjust (positive to credit, negative to debit)
     * @param notes            mandatory reason for the adjustment
     * @return the persisted ledger entry
     */
    PointsLedgerEntry adjust(String callerExternalId, UUID clientUuid, long points, String notes);
}
