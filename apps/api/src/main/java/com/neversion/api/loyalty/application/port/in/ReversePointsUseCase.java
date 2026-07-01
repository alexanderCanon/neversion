package com.neversion.api.loyalty.application.port.in;

/**
 * Reverses previously recorded points movements when their originating
 * reservation or order is rejected/cancelled.
 */
public interface ReversePointsUseCase {

    /**
     * Reverses any REDEEM entries tied to a reservation (restores the points
     * to the client's available balance). No-op if the reservation never
     * redeemed points.
     */
    void reverseForReservation(Long reservationId);

    /**
     * Reverses any EARN entries tied to an order. The reversal amount is
     * clamped to the client's current available balance — if the client
     * already spent some of the earned points, only the remaining portion
     * is reversed and a note is recorded for vendor visibility.
     */
    void reverseForOrder(Long orderId);
}
