package com.neversion.api.client.infrastructure.adapters.in.rest.dto;

/**
 * Response payload for GET /clients/{id}/deletion-check.
 * Informs the vendor about related data before confirming a soft delete.
 */
public record ClientDeletionCheckResponse(
        /** Total active subscriptions linked to this client. */
        long activeSubscriptions,
        /** Total pending or active reservations linked to this client. */
        long pendingReservations,
        /** Total orders (any status) linked to this client. */
        long totalOrders,
        /** True when the client has any blocking data that the vendor should review. */
        boolean hasRelatedData) {
}
