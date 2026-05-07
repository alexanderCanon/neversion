package com.neversion.api.dashboard.application.service;

import org.springframework.stereotype.Service;

import com.neversion.api.dashboard.application.port.in.GetActiveClientsKpiUseCase;
import com.neversion.api.dashboard.application.port.out.DashboardQueryPort;
import com.neversion.api.dashboard.application.result.ActiveClientsKpiResult;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@Service
public class GetActiveClientsKpiService implements GetActiveClientsKpiUseCase {

    private final DashboardQueryPort dashboardQueryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;

    public GetActiveClientsKpiService(
            DashboardQueryPort dashboardQueryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort) {
        this.dashboardQueryPort = dashboardQueryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    @Override
    public ActiveClientsKpiResult getForAuthenticatedVendor(String callerExternalId) {
        return new ActiveClientsKpiResult(
                dashboardQueryPort.countActiveClients(resolveVendorId(callerExternalId)));
    }

    private Long resolveVendorId(String callerExternalId) {
        var user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));
        return vendorRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found for userId: " + user.getId()))
                .getId();
    }
}
