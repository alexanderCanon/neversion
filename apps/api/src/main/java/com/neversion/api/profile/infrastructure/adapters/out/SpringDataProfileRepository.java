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
}
