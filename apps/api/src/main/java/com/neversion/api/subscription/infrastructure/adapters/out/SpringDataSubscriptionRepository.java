package com.neversion.api.subscription.infrastructure.adapters.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.neversion.api.subscription.domain.model.enums.SubStatus;

public interface SpringDataSubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    Optional<SubscriptionEntity> findByUuid(UUID uuid);

    Optional<SubscriptionEntity> findByOrderId(Long orderId);

    List<SubscriptionEntity> findByClientId(Long clientId);

    List<SubscriptionEntity> findByProfileId(Long profileId);

    List<SubscriptionEntity> findByStatus(SubStatus status);

    /**
     * US-043: Vendor-scoped list with optional filters. The service filter is
     * resolved through Profile -> Account because subscriptions anchor on profiles.
     */
    @Query(value = """
            SELECT s.*
            FROM subscriptions s
            JOIN profiles p ON p.id = s.profile_id
            JOIN accounts a ON a.id = p.account_id
            WHERE s.vendor_id = :vendorId
              AND (:serviceId IS NULL OR a.service_id = :serviceId)
              AND (:status IS NULL OR s.status = :status)
            ORDER BY s.payment_due_date ASC
            """, nativeQuery = true)
    List<SubscriptionEntity> findByVendorIdFiltered(
            @Param("vendorId") Long vendorId,
            @Param("serviceId") Long serviceId,
            @Param("status") String status);

    /** Overbooking guard (BR-04): a profile can only have one active subscription. */
    boolean existsByProfileIdAndStatus(Long profileId, SubStatus status);

    /**
     * Returns all subscriptions whose payment_due_date is on or before the given date.
     * Used by n8n to detect and process overdue payments (BR-10).
     */
    @Query("SELECT s FROM SubscriptionEntity s WHERE s.paymentDueDate <= :asOf AND s.status = :status")
    List<SubscriptionEntity> findOverdue(@Param("asOf") LocalDate asOf, @Param("status") SubStatus status);
}
