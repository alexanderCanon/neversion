package com.neversion.api.loyalty.application.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.loyalty.application.port.in.RedeemPointsUseCase;
import com.neversion.api.loyalty.domain.model.PointsLedgerEntry;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryStatus;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryType;
import com.neversion.api.loyalty.domain.port.out.PointsLedgerRepositoryPort;

/**
 * Redeems available points as a checkout discount. 1 point = 1 GTQ (BR: rewards program).
 */
@Service
public class RedeemPointsService implements RedeemPointsUseCase {

    private final PointsLedgerRepositoryPort pointsLedgerRepositoryPort;

    public RedeemPointsService(PointsLedgerRepositoryPort pointsLedgerRepositoryPort) {
        this.pointsLedgerRepositoryPort = pointsLedgerRepositoryPort;
    }

    @Override
    @Transactional
    public BigDecimal redeemForReservation(Long reservationId, Long clientId, Long vendorId, long pointsToRedeem) {
        if (pointsToRedeem <= 0) {
            throw new BusinessRuleException("pointsToRedeem must be greater than zero.");
        }

        long available = pointsLedgerRepositoryPort.sumByClientIdAndStatus(clientId, PointsEntryStatus.AVAILABLE);
        if (pointsToRedeem > available) {
            throw new BusinessRuleException(
                    "Insufficient points balance. Available: " + available + ", requested: " + pointsToRedeem);
        }

        pointsLedgerRepositoryPort.save(PointsLedgerEntry.builder()
                .clientId(clientId)
                .vendorId(vendorId)
                .reservationId(reservationId)
                .entryType(PointsEntryType.REDEEM)
                .status(PointsEntryStatus.AVAILABLE)
                .points(-pointsToRedeem)
                .notes("Redeemed at checkout for reservation #" + reservationId)
                .build());

        return BigDecimal.valueOf(pointsToRedeem);
    }
}
