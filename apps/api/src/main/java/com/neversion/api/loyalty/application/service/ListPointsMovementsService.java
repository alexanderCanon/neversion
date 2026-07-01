package com.neversion.api.loyalty.application.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.client.domain.model.Client;
import com.neversion.api.client.domain.port.out.ClientRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.loyalty.application.port.in.ListPointsMovementsUseCase;
import com.neversion.api.loyalty.application.port.in.dto.PointsMovementsPage;
import com.neversion.api.loyalty.domain.port.out.PointsLedgerRepositoryPort;
import com.neversion.api.shared.application.service.VendorSecurityService;
import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;

@Service
public class ListPointsMovementsService implements ListPointsMovementsUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    private final PointsLedgerRepositoryPort pointsLedgerRepositoryPort;
    private final VendorSecurityService vendorSecurityService;

    public ListPointsMovementsService(
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
    public PointsMovementsPage listForAuthenticatedClient(String callerExternalId, Pageable pageable) {
        User user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));

        Client client = clientRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client profile not found for authenticated user"));

        return pageFor(client.getId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PointsMovementsPage listForClientAsVendor(String callerExternalId, UUID clientUuid, Pageable pageable) {
        Vendor vendor = vendorSecurityService.resolveCallerVendor(callerExternalId);

        Client client = clientRepositoryPort.findById(clientUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientUuid));

        vendorSecurityService.assertOwnership(vendor.getId(), client.getVendorId(), "this client");

        return pageFor(client.getId(), pageable);
    }

    private PointsMovementsPage pageFor(Long clientId, Pageable pageable) {
        var movements = pointsLedgerRepositoryPort.findByClientId(clientId, pageable);
        long total = pointsLedgerRepositoryPort.countByClientId(clientId);
        return new PointsMovementsPage(movements, total);
    }
}
