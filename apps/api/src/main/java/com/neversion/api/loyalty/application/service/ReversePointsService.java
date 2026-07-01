package com.neversion.api.loyalty.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.loyalty.application.port.in.ReversePointsUseCase;
import com.neversion.api.loyalty.domain.model.PointsLedgerEntry;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryStatus;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryType;
import com.neversion.api.loyalty.domain.port.out.PointsLedgerRepositoryPort;

/**
 * Reverses points movements when their originating reservation/order is
 * rejected or cancelled. All reversal entries are recorded as REVERSAL type
 * so the ledger stays append-only and auditable.
 */
@Service
public class ReversePointsService implements ReversePointsUseCase {

    private final PointsLedgerRepositoryPort pointsLedgerRepositoryPort;

    public ReversePointsService(PointsLedgerRepositoryPort pointsLedgerRepositoryPort) {
        this.pointsLedgerRepositoryPort = pointsLedgerRepositoryPort;
    }

    @Override
    @Transactional
    public void reverseForReservation(Long reservationId) {
        List<PointsLedgerEntry> entries = pointsLedgerRepositoryPort.findByReservationId(reservationId);

        long redeemed = entries.stream()
                .filter(e -> e.getEntryType() == PointsEntryType.REDEEM
                        && e.getStatus() == PointsEntryStatus.AVAILABLE)
                .mapToLong(PointsLedgerEntry::getPoints)
                .sum(); // negative

        if (redeemed >= 0) {
            return; // nothing was redeemed
        }

        PointsLedgerEntry any = entries.get(0);
        pointsLedgerRepositoryPort.save(PointsLedgerEntry.builder()
                .clientId(any.getClientId())
                .vendorId(any.getVendorId())
                .reservationId(reservationId)
                .entryType(PointsEntryType.REVERSAL)
                .status(PointsEntryStatus.AVAILABLE)
                .points(-redeemed) // positive, restores the points
                .notes("Points restored - reservation #" + reservationId + " was rejected/cancelled")
                .build());
    }

    @Override
    @Transactional
    public void reverseForOrder(Long orderId) {
        List<PointsLedgerEntry> entries = pointsLedgerRepositoryPort.findByOrderId(orderId);

        long earned = entries.stream()
                .filter(e -> e.getEntryType() == PointsEntryType.EARN
                        && e.getStatus() == PointsEntryStatus.AVAILABLE)
                .mapToLong(PointsLedgerEntry::getPoints)
                .sum(); // positive

        if (earned <= 0) {
            return; // nothing was earned
        }

        PointsLedgerEntry any = entries.get(0);
        long available = pointsLedgerRepositoryPort.sumByClientIdAndStatus(
                any.getClientId(), PointsEntryStatus.AVAILABLE);

        long reversalAmount = Math.min(earned, Math.max(available, 0));
        if (reversalAmount <= 0) {
            return;
        }

        String notes = reversalAmount < earned
                ? "Points partially reversed - order #" + orderId
                        + " was cancelled but the client already spent part of the earned points"
                : "Points reversed - order #" + orderId + " was cancelled";

        pointsLedgerRepositoryPort.save(PointsLedgerEntry.builder()
                .clientId(any.getClientId())
                .vendorId(any.getVendorId())
                .orderId(orderId)
                .entryType(PointsEntryType.REVERSAL)
                .status(PointsEntryStatus.AVAILABLE)
                .points(-reversalAmount)
                .notes(notes)
                .build());
    }
}
