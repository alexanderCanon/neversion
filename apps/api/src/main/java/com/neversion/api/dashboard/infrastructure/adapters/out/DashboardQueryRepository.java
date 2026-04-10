package com.neversion.api.dashboard.infrastructure.adapters.out;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.neversion.api.dashboard.application.port.out.DashboardQueryPort;
import com.neversion.api.dashboard.application.result.ProfileResult;
import com.neversion.api.dashboard.application.result.ProductSummaryResult;
import com.neversion.api.dashboard.application.result.ProfileCustomerResult;
import com.neversion.api.dashboard.application.result.ProfileSubscriptionResult;

/**
 * JdbcTemplate-based implementation of {@link DashboardQueryPort}.
 * Read-only cross-table projections for the dashboard feature.
 */
@Repository
public class DashboardQueryRepository implements DashboardQueryPort {

    private final JdbcTemplate jdbcTemplate;

    public DashboardQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ProductSummaryResult> findProductsByCategory(String category) {
        String sql = """
                SELECT s.id         AS product_id,
                       s.name       AS product_name,
                       s.category   AS category,
                       COUNT(DISTINCT a.id) AS total_accounts
                FROM services s
                LEFT JOIN accounts a ON a.service_id = s.id
                WHERE s.category = ?
                GROUP BY s.id, s.name, s.category
                ORDER BY s.name
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ProductSummaryResult(
                rs.getObject("product_id", UUID.class),
                rs.getString("product_name"),
                rs.getString("category").toUpperCase(),
                rs.getInt("total_accounts")), category);
    }

    @Override
    public List<Map<String, Object>> findAccountsByProductId(UUID productId) {
        String sql = """
                SELECT a.uuid              AS account_id,
                       a.email             AS email,
                       a.password          AS password,
                       a.renewal_date      AS cut_off_date,
                       CASE WHEN a.sale_mode = 'BY_PROFILE' THEN 'FAMILY' ELSE 'INDIVIDUAL' END
                                           AS account_type,
                       CASE WHEN a.renewal_date >= CURRENT_DATE THEN 'ACTIVE' ELSE 'EXPIRED' END
                                           AS account_status,
                       svc.max_profiles    AS max_profiles,
                       COUNT(DISTINCT CASE WHEN s.status IN ('ACTIVE', 'SUSPENDED') THEN p.id END)
                                           AS occupied_profiles
                FROM accounts a
                JOIN services svc ON a.service_id = svc.id
                LEFT JOIN profiles p ON p.account_id = a.id
                LEFT JOIN subscriptions s ON s.profile_id = p.id
                     AND s.status IN ('ACTIVE', 'SUSPENDED')
                WHERE svc.uuid = ?
                GROUP BY a.uuid, a.email, a.password, a.renewal_date, a.sale_mode, svc.max_profiles
                ORDER BY a.renewal_date
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("accountId", rs.getObject("account_id", UUID.class));
            row.put("email", rs.getString("email"));
            row.put("password", rs.getString("password"));
            row.put("cutOffDate", rs.getObject("cut_off_date", LocalDate.class));
            row.put("accountType", rs.getString("account_type"));
            row.put("accountStatus", rs.getString("account_status"));
            row.put("maxProfiles", rs.getInt("max_profiles"));
            row.put("occupiedProfiles", rs.getInt("occupied_profiles"));
            return row;
        }, productId);
    }

    @Override
    public List<ProfileResult> findProfilesByAccountId(UUID accountId) {
        String sql = """
                SELECT sl.uuid              AS profile_id,
                       sl.name              AS profile_name,
                       sl.pin               AS pin,
                       CASE
                           WHEN s.status = 'ACTIVE'    THEN 'OCCUPIED'
                           WHEN s.status = 'SUSPENDED' THEN 'BLOCKED'
                           ELSE 'AVAILABLE'
                       END                  AS profile_status,
                       s.uuid               AS sub_id,
                       s.start_date         AS start_date,
                       s.payment_due_date   AS end_date,
                       s.status             AS sub_status,
                       c.uuid               AS customer_id,
                       c.name               AS customer_name,
                       c.phone              AS customer_phone
                FROM profiles sl
                LEFT JOIN subscriptions s ON s.profile_id = sl.id
                     AND s.status IN ('ACTIVE', 'SUSPENDED')
                LEFT JOIN clients c ON c.id = s.client_id
                WHERE sl.account_id = (SELECT id FROM accounts WHERE uuid = ?)
                ORDER BY sl.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UUID subId = rs.getObject("sub_id", UUID.class);
            ProfileSubscriptionResult subscription = null;

            if (subId != null) {
                LocalDate endDate = rs.getObject("end_date", LocalDate.class);
                String rawStatus = rs.getString("sub_status").toUpperCase();

                // BR-10: EXPIRING_SOON calculated dynamically
                String status = rawStatus;
                if ("ACTIVE".equals(rawStatus) && endDate != null) {
                    LocalDate threshold = LocalDate.now().plusDays(7);
                    if (!endDate.isAfter(threshold)) {
                        status = "EXPIRING_SOON";
                    }
                }

                UUID customerId = rs.getObject("customer_id", UUID.class);
                ProfileCustomerResult customer = customerId != null
                        ? new ProfileCustomerResult(
                                customerId,
                                rs.getString("customer_name"),
                                rs.getString("customer_phone"),
                                "CLIENT")
                        : null;

                subscription = new ProfileSubscriptionResult(
                        subId,
                        rs.getObject("start_date", LocalDate.class),
                        endDate,
                        status,
                        customer);
            }

            return new ProfileResult(
                    rs.getObject("profile_id", UUID.class),
                    rs.getString("profile_name"),
                    rs.getString("pin"),
                    rs.getString("profile_status"),
                    subscription);
        }, accountId);
    }
}
