package com.neversion.api.dashboard.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.neversion.api.dashboard.application.port.in.GetAccountsByProductUseCase;
import com.neversion.api.dashboard.application.port.out.DashboardQueryPort;
import com.neversion.api.dashboard.application.result.AccountGroupResult;
import com.neversion.api.dashboard.domain.model.enums.AccountAvailability;

/**
 * Service for endpoint 2. Calculates AccountAvailability (BR-06)
 * from raw DB data — never persisted.
 */
@Service
public class GetAccountsByProductService implements GetAccountsByProductUseCase {

    private final DashboardQueryPort dashboardQueryPort;

    public GetAccountsByProductService(DashboardQueryPort dashboardQueryPort) {
        this.dashboardQueryPort = dashboardQueryPort;
    }

    @Override
    public List<AccountGroupResult> getByProductId(UUID productId) {
        List<Map<String, Object>> rawAccounts = dashboardQueryPort.findAccountsByProductId(productId);
        return rawAccounts.stream().map(this::toAccountGroupResult).toList();
    }

    private AccountGroupResult toAccountGroupResult(Map<String, Object> row) {
        UUID accountId = (UUID) row.get("accountId");
        String email = (String) row.get("email");
        String password = (String) row.get("password");
        LocalDate cutOffDate = row.get("cutOffDate") != null
                ? LocalDate.parse(row.get("cutOffDate").toString())
                : null;
        String accountType = ((String) row.get("accountType")).toUpperCase();
        String accountStatus = ((String) row.get("accountStatus")).toUpperCase();
        int maxProfiles = ((Number) row.get("maxProfiles")).intValue();
        int occupiedProfiles = ((Number) row.get("occupiedProfiles")).intValue();
        int availableProfiles = maxProfiles - occupiedProfiles;

        String availability = calculateAvailability(accountType, maxProfiles, occupiedProfiles, availableProfiles);

        return new AccountGroupResult(
                accountId, email, password, cutOffDate,
                accountType, accountStatus, maxProfiles,
                occupiedProfiles, availableProfiles, availability);
    }

    /**
     * BR-06: AccountAvailability is calculated at query time.
     * <ul>
     *   <li>If accountType = INDIVIDUAL → INDIVIDUAL</li>
     *   <li>If accountType = FAMILY and 1 subscription occupies entire account → COMPLETE</li>
     *   <li>If accountType = FAMILY and availableProfiles = 0 → NO_AVAILABILITY</li>
     *   <li>If accountType = FAMILY and availableProfiles > 0 and occupiedProfiles > 0 → PARTIAL</li>
     *   <li>Otherwise (all free) → PARTIAL</li>
     * </ul>
     */
    private String calculateAvailability(String accountType, int maxProfiles, int occupiedProfiles, int availableProfiles) {
        if ("INDIVIDUAL".equals(accountType)) {
            return AccountAvailability.INDIVIDUAL.name();
        }
        if (availableProfiles == 0) {
            return AccountAvailability.NO_AVAILABILITY.name();
        }
        if (occupiedProfiles > 0) {
            return AccountAvailability.PARTIAL.name();
        }
        // All profiles free — still PARTIAL (has free profiles)
        return AccountAvailability.PARTIAL.name();
    }
}
