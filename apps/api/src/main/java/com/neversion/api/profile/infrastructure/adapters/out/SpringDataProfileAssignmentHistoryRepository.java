package com.neversion.api.profile.infrastructure.adapters.out;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for profile_assignment_history.
 * Backed by the V32 migration.
 */
public interface SpringDataProfileAssignmentHistoryRepository
        extends JpaRepository<ProfileAssignmentHistoryEntity, Long> {

    /**
     * Finds the open (releasedAt IS NULL) assignment record for the given profile.
     * Used by RevokeSubscriptionService to close the audit entry on cancellation.
     */
    Optional<ProfileAssignmentHistoryEntity> findByProfileIdAndReleasedAtIsNull(Long profileId);
}
