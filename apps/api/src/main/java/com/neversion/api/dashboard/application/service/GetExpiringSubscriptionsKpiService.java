package com.neversion.api.dashboard.application.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.neversion.api.dashboard.application.port.in.GetExpiringSubscriptionsKpiUseCase;
import com.neversion.api.dashboard.application.port.out.DashboardQueryPort;
import com.neversion.api.dashboard.application.result.ExpiringSubscriptionResult;
import com.neversion.api.dashboard.application.result.ExpiringSubscriptionsKpiResult;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@Service
public class GetExpiringSubscriptionsKpiService implements GetExpiringSubscriptionsKpiUseCase {

    private static final int WEEK_WINDOW_DAYS = 6;

    private final DashboardQueryPort dashboardQueryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final Clock clock;

    public GetExpiringSubscriptionsKpiService(
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
    public ExpiringSubscriptionsKpiResult getForAuthenticatedVendor(String callerExternalId) {
        Long vendorId = resolveVendorId(callerExternalId);
        LocalDate today = LocalDate.now(clock);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate weekEnd = today.plusDays(WEEK_WINDOW_DAYS);

        List<ExpiringSubscriptionResult> subscriptions =
                dashboardQueryPort.findExpiringSubscriptions(vendorId, today, weekEnd);

        return new ExpiringSubscriptionsKpiResult(
                filterByDate(subscriptions, today),
                filterByDate(subscriptions, tomorrow),
                subscriptions.stream()
                        .filter(subscription -> subscription.paymentDueDate() != null)
                        .filter(subscription -> subscription.paymentDueDate().isAfter(tomorrow))
                        .filter(subscription -> !subscription.paymentDueDate().isAfter(weekEnd))
                        .toList());
    }

    private List<ExpiringSubscriptionResult> filterByDate(
            List<ExpiringSubscriptionResult> subscriptions,
            LocalDate dueDate) {
        return subscriptions.stream()
                .filter(subscription -> dueDate.equals(subscription.paymentDueDate()))
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
