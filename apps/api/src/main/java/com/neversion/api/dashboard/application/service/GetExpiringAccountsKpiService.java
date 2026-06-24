package com.neversion.api.dashboard.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.neversion.api.dashboard.application.port.in.GetExpiringAccountsKpiUseCase;
import com.neversion.api.dashboard.application.port.out.DashboardQueryPort;
import com.neversion.api.dashboard.application.result.ExpiringAccountResult;
import com.neversion.api.dashboard.application.result.ExpiringAccountsKpiResult;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@Service
public class GetExpiringAccountsKpiService implements GetExpiringAccountsKpiUseCase {

    private static final int WEEK_WINDOW_DAYS = 6;

    private final DashboardQueryPort dashboardQueryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final Clock clock;

    public GetExpiringAccountsKpiService(
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
    public ExpiringAccountsKpiResult getForAuthenticatedVendor(String callerExternalId) {
        Long vendorId = resolveVendorId(callerExternalId);
        LocalDate today = LocalDate.now(clock);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate weekEnd = today.plusDays(WEEK_WINDOW_DAYS);

        List<ExpiringAccountResult> accounts =
                dashboardQueryPort.findExpiringAccounts(vendorId, today, weekEnd);

        return new ExpiringAccountsKpiResult(
                filterByDate(accounts, today),
                filterByDate(accounts, tomorrow),
                accounts.stream()
                        .filter(account -> account.renewalDate() != null)
                        .filter(account -> account.renewalDate().isAfter(tomorrow))
                        .filter(account -> !account.renewalDate().isAfter(weekEnd))
                        .toList());
    }

    private List<ExpiringAccountResult> filterByDate(
            List<ExpiringAccountResult> accounts,
            LocalDate renewalDate) {
        return accounts.stream()
                .filter(account -> renewalDate.equals(account.renewalDate()))
                .toList();
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
