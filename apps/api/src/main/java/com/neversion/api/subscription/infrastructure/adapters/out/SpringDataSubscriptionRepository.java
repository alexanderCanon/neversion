package com.neversion.api.subscription.infrastructure.adapters.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.neversion.api.subscription.domain.model.SubscriptionListView;
import com.neversion.api.subscription.domain.model.enums.SubStatus;

public interface SpringDataSubscriptionRepository
        extends JpaRepository<SubscriptionEntity, Long>, JpaSpecificationExecutor<SubscriptionEntity> {

    Optional<SubscriptionEntity> findByUuid(UUID uuid);

    Optional<SubscriptionEntity> findByOrderId(Long orderId);

    List<SubscriptionEntity> findByClientId(Long clientId);

    List<SubscriptionEntity> findByProfileId(Long profileId);

    List<SubscriptionEntity> findByStatus(SubStatus status);

    /** Overbooking guard (BR-04): a profile can only have one active subscription. */
    boolean existsByProfileIdAndStatus(Long profileId, SubStatus status);

    /**
     * Returns all subscriptions whose payment_due_date is on or before the given date.
     * Used by n8n to detect and process overdue payments (BR-10).
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE s.paymentDueDate <= :asOf AND s.status = :status")
    List<SubscriptionEntity> findOverdue(@Param("asOf") LocalDate asOf, @Param("status") SubStatus status);

    /** US-054: Finds active subscriptions due on a specific date for renewal reminders. */
    List<SubscriptionEntity> findByPaymentDueDateAndStatus(LocalDate paymentDueDate, SubStatus status);

    /**
     * US-043 / tech-debt A3: Single-query projection for the vendor subscription
     * list. Uses Hibernate ad-hoc entity joins (the FK columns are plain Long
     * fields, not mapped associations) to avoid the previous N+1 enrichment.
     */
    @Query("""
            SELECT new com.neversion.api.subscription.domain.model.SubscriptionListView(
                s.uuid, p.uuid, p.name, c.uuid, c.name, acc.uuid, svc.name,
                s.status, s.startDate, s.endDate, s.paymentDueDate, s.monthsPaid, s.notes, s.createdAt)
            FROM SubscriptionEntity s
            JOIN ProfileEntity p ON p.id = s.profileId
            JOIN ClientEntity c ON c.id = s.clientId
            JOIN AccountEntity acc ON acc.id = p.accountId
            JOIN ServiceEntity svc ON svc.id = acc.serviceId
            WHERE s.vendorId = :vendorId
              AND (:serviceId IS NULL OR acc.serviceId = :serviceId)
              AND (:status IS NULL OR s.status = :status)
            ORDER BY s.paymentDueDate ASC
            """)
    List<SubscriptionListView> findVendorSubscriptionViews(
            @Param("vendorId") Long vendorId,
            @Param("serviceId") Long serviceId,
            @Param("status") SubStatus status);
}
