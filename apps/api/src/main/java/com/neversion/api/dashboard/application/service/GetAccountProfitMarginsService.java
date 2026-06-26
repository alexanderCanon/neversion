package com.neversion.api.dashboard.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.neversion.api.dashboard.application.port.in.GetAccountProfitMarginsUseCase;
import com.neversion.api.dashboard.application.port.out.DashboardQueryPort;
import com.neversion.api.dashboard.application.result.AccountProfitMarginResult;
import com.neversion.api.dashboard.application.result.ProfitMarginsResult;
import com.neversion.api.dashboard.application.result.ServiceProfitSummaryResult;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;

@Service
public class GetAccountProfitMarginsService implements GetAccountProfitMarginsUseCase {

    private static final String CURRENCY_GTQ = "GTQ";

    private final DashboardQueryPort dashboardQueryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;
    private final Clock clock;

    public GetAccountProfitMarginsService(
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
    public ProfitMarginsResult getForAuthenticatedVendor(String callerExternalId, Integer year, Integer month) {
        Long vendorId = resolveVendorId(callerExternalId);

        YearMonth ym = (year != null && month != null)
                ? YearMonth.of(year, month)
                : YearMonth.now(clock);

        LocalDate periodStart = ym.atDay(1);
        OffsetDateTime startUtc = periodStart.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endUtc = ym.plusMonths(1).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<AccountProfitMarginResult> accounts =
                dashboardQueryPort.findAccountProfitMargins(vendorId, startUtc, endUtc);

        List<ServiceProfitSummaryResult> serviceSummaries = aggregateByService(accounts);
        ProfitMarginsResult.GrandTotal grandTotal = aggregateGrandTotal(accounts);

        return new ProfitMarginsResult(
                periodStart,
                ym.atEndOfMonth(),
                CURRENCY_GTQ,
                serviceSummaries,
                accounts,
                grandTotal);
    }

    private List<ServiceProfitSummaryResult> aggregateByService(List<AccountProfitMarginResult> accounts) {
        Map<String, List<AccountProfitMarginResult>> byService = new LinkedHashMap<>();
        for (AccountProfitMarginResult a : accounts) {
            byService.computeIfAbsent(a.serviceName(), k -> new ArrayList<>()).add(a);
        }

        List<ServiceProfitSummaryResult> summaries = new ArrayList<>();
        for (Map.Entry<String, List<AccountProfitMarginResult>> entry : byService.entrySet()) {
            List<AccountProfitMarginResult> svcAccounts = entry.getValue();
            int activeAccounts = svcAccounts.size();
            int totalProfilesSold = svcAccounts.stream()
                    .mapToInt(AccountProfitMarginResult::profilesSold)
                    .sum();
            BigDecimal totalRevenue = sum(svcAccounts, AccountProfitMarginResult::totalRevenue);
            BigDecimal totalAllocatedCost = sum(svcAccounts, AccountProfitMarginResult::allocatedCost);
            BigDecimal totalProfit = totalRevenue.subtract(totalAllocatedCost);
            BigDecimal avgMarginPct = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? totalProfit.multiply(BigDecimal.valueOf(100))
                            .divide(totalRevenue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            summaries.add(new ServiceProfitSummaryResult(
                    entry.getKey(),
                    activeAccounts,
                    totalProfilesSold,
                    totalRevenue,
                    totalAllocatedCost,
                    totalProfit,
                    avgMarginPct));
        }
        return summaries;
    }

    private ProfitMarginsResult.GrandTotal aggregateGrandTotal(List<AccountProfitMarginResult> accounts) {
        BigDecimal totalRevenue = sum(accounts, AccountProfitMarginResult::totalRevenue);
        BigDecimal totalAllocatedCost = sum(accounts, AccountProfitMarginResult::allocatedCost);
        BigDecimal totalProfit = totalRevenue.subtract(totalAllocatedCost);
        BigDecimal avgMarginPct = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalProfit.multiply(BigDecimal.valueOf(100))
                        .divide(totalRevenue, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new ProfitMarginsResult.GrandTotal(totalRevenue, totalAllocatedCost, totalProfit, avgMarginPct);
    }

    private static BigDecimal sum(List<AccountProfitMarginResult> list,
            java.util.function.Function<AccountProfitMarginResult, BigDecimal> extractor) {
        return list.stream()
                .map(extractor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
