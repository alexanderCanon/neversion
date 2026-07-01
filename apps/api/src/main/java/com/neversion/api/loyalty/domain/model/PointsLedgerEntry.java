package com.neversion.api.loyalty.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.neversion.api.loyalty.domain.model.enums.PointsEntryStatus;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain model for a single movement in a client's loyalty points ledger.
 * Append-only — balances are derived by summing {@code points} per client/status.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsLedgerEntry {

    private Long id;
    private UUID uuid;

    /** FK to clients.id. */
    private Long clientId;

    /** FK to vendors.id — multi-tenancy. */
    private Long vendorId;

    /** FK to orders.id — set for EARN entries and their REVERSAL. */
    private Long orderId;

    /** FK to reservations.id — set for REDEEM entries and their REVERSAL. */
    private Long reservationId;

    private PointsEntryType entryType;

    @Builder.Default
    private PointsEntryStatus status = PointsEntryStatus.AVAILABLE;

    /** Positive for credits (EARN, REVERSAL, positive ADJUSTMENT), negative for debits (REDEEM, negative ADJUSTMENT). */
    private Long points;

    private String notes;

    /** Supabase externalId of the vendor user who made a manual ADJUSTMENT, null otherwise. */
    private String createdBy;

    private Instant createdAt;
}
