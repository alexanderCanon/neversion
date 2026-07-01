package com.neversion.api.loyalty.application.port.in;

import java.math.BigDecimal;

/**
 * Redeems available points as a discount applied at checkout (1 point = 1 GTQ).
 */
public interface RedeemPointsUseCase {

    /**
     * Validates that the client has enough available points and records the
     * REDEEM movement linked to the reservation.
     *
     * @param reservationId internal reservation id (FK for the ledger entry)
     * @param clientId      internal client id redeeming the points
     * @param vendorId      internal vendor id (tenancy)
     * @param pointsToRedeem number of points to redeem (must be > 0)
     * @return the discount amount in GTQ (equal to pointsToRedeem)
     */
    BigDecimal redeemForReservation(Long reservationId, Long clientId, Long vendorId, long pointsToRedeem);
}
