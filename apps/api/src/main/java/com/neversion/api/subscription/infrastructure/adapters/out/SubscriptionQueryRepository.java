package com.neversion.api.subscription.infrastructure.adapters.out;

import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.stereotype.Repository;
import com.neversion.api.subscription.domain.model.SubscriptionListView;
import com.neversion.api.subscription.domain.model.enums.SubStatus;

@Repository
public class SubscriptionQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SubscriptionQueryRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SubscriptionListView> findVendorSubscriptionViews(Long vendorId, Long serviceId, SubStatus status) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                s.uuid AS subscriptionUuid, 
                p.uuid AS profileUuid, 
                p.name AS profileName, 
                c.uuid AS clientUuid, 
                c.name AS clientName, 
                acc.uuid AS accountUuid, 
                svc.name AS serviceName,
                s.status, 
                s.start_date AS startDate, 
                s.end_date AS endDate, 
                s.payment_due_date AS paymentDueDate, 
                s.months_paid AS monthsPaid, 
                s.notes, 
                s.created_at AS createdAt
            FROM subscriptions s
            INNER JOIN profiles p ON p.id = s.profile_id
            INNER JOIN clients c ON c.id = s.client_id AND c.deleted_at IS NULL
            INNER JOIN accounts acc ON acc.id = p.account_id
            INNER JOIN services svc ON svc.id = acc.service_id
            WHERE s.vendor_id = :vendorId
        """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("vendorId", vendorId);

        if (serviceId != null) {
            sql.append(" AND acc.service_id = :serviceId");
            params.addValue("serviceId", serviceId);
        }
        if (status != null) {
            sql.append(" AND s.status = :status");
            params.addValue("status", status.name());
        }

        sql.append(" ORDER BY s.payment_due_date ASC");

        return jdbcTemplate.query(
            sql.toString(), 
            params, 
            new DataClassRowMapper<>(SubscriptionListView.class)
        );
    }
}
