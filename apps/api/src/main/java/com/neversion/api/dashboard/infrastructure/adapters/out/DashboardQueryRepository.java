package com.neversion.api.dashboard.infrastructure.adapters.out;

import java.time.LocalDate;
import java.util.ArrayList;
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
                SELECT p.id         AS product_id,
                       p.name       AS product_name,
                       p.category::text AS category,
                       COUNT(DISTINCT a.id) AS total_accounts
                FROM products p
                JOIN inventory i ON i.product_id = p.id AND i.is_active = true
                JOIN accounts a  ON a.inventory_id = i.id AND a.is_active = true
                WHERE p.category = ?::category_type
                  AND p.is_active = true
                GROUP BY p.id, p.name, p.category
                ORDER BY p.name
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
                SELECT a.id                AS account_id,
                       a.email             AS email,
                       a.pass              AS password,
                       a.expiration_date   AS cut_off_date,
                       i.account_type::text AS account_type,
                       a.status::text      AS account_status,
                       COALESCE(i.max_profiles, 1) AS max_profiles,
                       COUNT(s.id) FILTER (WHERE s.status::text IN ('active', 'expired')
                           AND s.renewal_date >= CURRENT_DATE) AS occupied_profiles
                FROM accounts a
                JOIN inventory i ON a.inventory_id = i.id
                LEFT JOIN profiles sl ON sl.account_id = a.id
                LEFT JOIN subscriptions s ON s.profile_id = sl.id
                     AND s.status::text IN ('active')
                WHERE i.product_id = ?
                  AND a.is_active = true
                  AND i.is_active = true
                GROUP BY a.id, a.email, a.pass, a.expiration_date,
                         i.account_type, a.status, i.max_profiles
                ORDER BY a.expiration_date
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
                SELECT sl.id             AS profile_id,
                       sl.profile_name   AS profile_name,
                       sl.pin            AS pin,
                       sl.status::text   AS profile_status,
                       s.id              AS sub_id,
                       s.purchase_date   AS start_date,
                       s.renewal_date    AS end_date,
                       s.status::text    AS sub_status,
                       ug.id             AS customer_id,
                       ug.name           AS customer_name,
                       ug.phone          AS customer_phone
                FROM profiles sl
                LEFT JOIN subscriptions s ON s.profile_id = sl.id
                     AND s.status::text IN ('active', 'expired', 'cancelled', 'suspended')
                LEFT JOIN users_guests ug ON s.user_guest_id = ug.id
                WHERE sl.account_id = ?
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
                                "USER_GUEST")
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
                    rs.getString("profile_status").toUpperCase(),
                    subscription);
        }, accountId);
    }
}
