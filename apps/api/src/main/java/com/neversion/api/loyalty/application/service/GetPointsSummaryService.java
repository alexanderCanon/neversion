package com.neversion.api.loyalty.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.loyalty.application.port.in.GetPointsSummaryUseCase;
import com.neversion.api.loyalty.application.port.in.dto.PointsSummary;
import com.neversion.api.loyalty.domain.model.enums.PointsEntryStatus;
import com.neversion.api.loyalty.domain.port.out.PointsLedgerRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;

@Service
public class GetPointsSummaryService implements GetPointsSummaryUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final PointsLedgerRepositoryPort pointsLedgerRepositoryPort;
    private final VendorSecurityService vendorSecurityService;

    public GetPointsSummaryService(
            UserRepositoryPort userRepositoryPort,
            ClientRepositoryPort clientRepositoryPort,
            PointsLedgerRepositoryPort pointsLedgerRepositoryPort,
            VendorSecurityService vendorSecurityService) {
        this.userRepositoryPort = userRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
        this.pointsLedgerRepositoryPort = pointsLedgerRepositoryPort;
        this.vendorSecurityService = vendorSecurityService;
    }

    @Override
    @Transactional(readOnly = true)
    public PointsSummary getForAuthenticatedClient(String callerExternalId) {
        User user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));

        Client client = clientRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client profile not found for authenticated user"));

        return summaryFor(client.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public PointsSummary getForClientAsVendor(String callerExternalId, UUID clientUuid) {
        Vendor vendor = vendorSecurityService.resolveCallerVendor(callerExternalId);

        Client client = clientRepositoryPort.findById(clientUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientUuid));

        vendorSecurityService.assertOwnership(vendor.getId(), client.getVendorId(), "this client");

        return summaryFor(client.getId());
    }

    private PointsSummary summaryFor(Long clientId) {
        long available = pointsLedgerRepositoryPort.sumByClientIdAndStatus(clientId, PointsEntryStatus.AVAILABLE);
        long pending = pointsLedgerRepositoryPort.sumByClientIdAndStatus(clientId, PointsEntryStatus.PENDING);
        return PointsSummary.of(available, pending);
    }
}
