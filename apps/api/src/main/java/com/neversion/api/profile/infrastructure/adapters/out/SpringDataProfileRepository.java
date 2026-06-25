package com.neversion.api.profile.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataProfileRepository extends JpaRepository<ProfileEntity, Long> {

    Optional<ProfileEntity> findByUuid(UUID uuid);

    List<ProfileEntity> findByAccountId(Long accountId);

    /**
     * Returns profiles not linked to any active subscription (BR-04).
     * A profile is "available" when it has no subscription with status = 'active'.
     */
    @Query("""
            SELECT p FROM ProfileEntity p
            WHERE p.accountId = :accountId
              AND NOT EXISTS (
                  SELECT 1 FROM SubscriptionEntity s
                  WHERE s.profileId = p.id
                    AND s.status = 'ACTIVE'
              )
            """)
    List<ProfileEntity> findAvailableByAccountId(@Param("accountId") Long accountId);

    /**
     * US-033: Count AVAILABLE profiles for a service across all vendor accounts.
     * Uses native query to join profiles → accounts → filter by service_id + vendor_id.
     */
    @Query(value = "SELECT COUNT(*) FROM profiles p "
            + "JOIN accounts a ON p.account_id = a.id "
            + "WHERE a.service_id = :serviceId "
            + "AND a.vendor_id = :vendorId "
            + "AND p.status = 'available'",
            nativeQuery = true)
    long countAvailableByServiceIdAndVendorId(@Param("serviceId") Long serviceId,
                                              @Param("vendorId") Long vendorId);

    /**
     * Returns AVAILABLE profiles for a service across all vendor accounts,
     * ordered by creation date ASC. Used by batch subscription auto-assignment.
     */
    @Query(value = "SELECT p.* FROM profiles p "
            + "JOIN accounts a ON p.account_id = a.id "
            + "WHERE a.service_id = :serviceId "
            + "AND a.vendor_id = :vendorId "
            + "AND p.status = 'available' "
            + "ORDER BY p.created_at ASC",
            nativeQuery = true)
    List<ProfileEntity> findAvailableByServiceIdAndVendorId(@Param("serviceId") Long serviceId,
                                                            @Param("vendorId") Long vendorId);
}
