package com.neversion.api.dashboard.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;

import com.neversion.api.dashboard.application.port.in.GetSuccessfulRenewalsKpiUseCase;
import com.neversion.api.dashboard.application.port.out.DashboardQueryPort;
import com.neversion.api.dashboard.application.result.SuccessfulRenewalsKpiResult;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@Service
public class GetSuccessfulRenewalsKpiService implements GetSuccessfulRenewalsKpiUseCase {

    private final DashboardQueryPort dashboardQueryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final Clock clock;

    public GetSuccessfulRenewalsKpiService(
            DashboardQueryPort dashboardQueryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort,
            Clock clock) {
        this.dashboardQueryPort = dashboardQueryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
        this.clock = clock;
    }

    @Override
    public SuccessfulRenewalsKpiResult getForAuthenticatedVendor(String callerExternalId) {
        Long vendorId = resolveVendorId(callerExternalId);
        LocalDate periodStartDate = LocalDate.now(clock).withDayOfMonth(1);
        OffsetDateTime periodStart = periodStartDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime nextPeriodStart = periodStartDate.plusMonths(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        return new SuccessfulRenewalsKpiResult(
                dashboardQueryPort.countSuccessfulRenewals(vendorId, periodStart, nextPeriodStart));
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
