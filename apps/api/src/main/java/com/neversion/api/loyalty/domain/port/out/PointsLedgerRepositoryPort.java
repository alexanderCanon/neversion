package com.neversion.api.loyalty.domain.port.out;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.neversion.api.loyalty.domain.model.PointsLedgerEntry;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryStatus;

/**
 * Outbound port — contract for the client points ledger persistence.
 * The ledger is append-only: balances are derived, never updated in place.
 */
public interface PointsLedgerRepositoryPort {

    PointsLedgerEntry save(PointsLedgerEntry entry);

    /** Sum of points for a client filtered by status (e.g. AVAILABLE balance). */
    long sumByClientIdAndStatus(Long clientId, PointsEntryStatus status);

    List<PointsLedgerEntry> findByClientId(Long clientId, Pageable pageable);

    long countByClientId(Long clientId);

    List<PointsLedgerEntry> findByOrderId(Long orderId);

    List<PointsLedgerEntry> findByReservationId(Long reservationId);
}
