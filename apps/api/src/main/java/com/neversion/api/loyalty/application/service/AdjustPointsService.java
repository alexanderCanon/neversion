package com.neversion.api.loyalty.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.loyalty.application.port.in.AdjustPointsUseCase;
import com.neversion.api.loyalty.domain.model.PointsLedgerEntry;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryStatus;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryType;
import com.neversion.api.loyalty.domain.port.out.PointsLedgerRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.vendor.domain.model.Vendor;

/**
 * Manual points adjustment made by the vendor from the panel (+/-).
 */
@Service
public class AdjustPointsService implements AdjustPointsUseCase {

    private final VendorSecurityService vendorSecurityService;
    private final ClientRepositoryPort clientRepositoryPort;
    private final PointsLedgerRepositoryPort pointsLedgerRepositoryPort;

    public AdjustPointsService(
            VendorSecurityService vendorSecurityService,
            ClientRepositoryPort clientRepositoryPort,
            PointsLedgerRepositoryPort pointsLedgerRepositoryPort) {
        this.vendorSecurityService = vendorSecurityService;
        this.clientRepositoryPort = clientRepositoryPort;
        this.pointsLedgerRepositoryPort = pointsLedgerRepositoryPort;
    }

    @Override
    @Transactional
    public PointsLedgerEntry adjust(String callerExternalId, UUID clientUuid, long points, String notes) {
        if (points == 0) {
            throw new BusinessRuleException("points must not be zero.");
        }
        if (notes == null || notes.isBlank()) {
            throw new BusinessRuleException("A reason (notes) is mandatory for manual point adjustments.");
        }

        Vendor vendor = vendorSecurityService.resolveCallerVendor(callerExternalId);

        Client client = clientRepositoryPort.findById(clientUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientUuid));

        vendorSecurityService.assertOwnership(vendor.getId(), client.getVendorId(), "this client");

        if (points < 0) {
            long available = pointsLedgerRepositoryPort.sumByClientIdAndStatus(
                    client.getId(), PointsEntryStatus.AVAILABLE);
            if (-points > available) {
                throw new BusinessRuleException(
                        "Insufficient points balance. Available: " + available + ", requested debit: " + (-points));
            }
        }

        return pointsLedgerRepositoryPort.save(PointsLedgerEntry.builder()
                .clientId(client.getId())
                .vendorId(vendor.getId())
                .entryType(PointsEntryType.ADJUSTMENT)
                .status(PointsEntryStatus.AVAILABLE)
                .points(points)
                .notes(notes)
                .createdBy(callerExternalId)
                .build());
    }
}
