package com.neversion.api.profile.domain.port.out;

import java.util.Optional;

import com.neversion.api.profile.domain.model.ProfileAssignmentHistory;

/**
 * Output port for persisting and querying profile assignment history records.
 * Implemented in the infrastructure layer by JpaProfileAssignmentHistoryAdapter.
 */
public interface ProfileAssignmentHistoryRepositoryPort {

    /**
     * Persists a new assignment history record (on subscription activation)
     * or updates an existing one (on release / close).
     */
    ProfileAssignmentHistory save(ProfileAssignmentHistory history);

    /**
     * Finds the open (not-yet-released) assignment entry for a given profile.
     * Used on subscription cancellation to close the record and capture the
     * release timestamp.
     *
     * @param profileId internal DB id of the profile slot
     * @return the open assignment, or empty if none exists
     */
    Optional<ProfileAssignmentHistory> findOpenByProfileId(Long profileId);
}
