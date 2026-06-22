package com.neversion.api.profile.infrastructure.adapters.out;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.neversion.api.profile.domain.model.ProfileAssignmentHistory;
import com.neversion.api.profile.domain.port.out.ProfileAssignmentHistoryRepositoryPort;

/**
 * JPA adapter that implements ProfileAssignmentHistoryRepositoryPort.
 * Bridges between the domain model and the JPA entity / Spring Data repository.
 */
@Component
public class JpaProfileAssignmentHistoryAdapter implements ProfileAssignmentHistoryRepositoryPort {

    private final SpringDataProfileAssignmentHistoryRepository repository;

    public JpaProfileAssignmentHistoryAdapter(
            SpringDataProfileAssignmentHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProfileAssignmentHistory save(ProfileAssignmentHistory history) {
        ProfileAssignmentHistoryEntity entity = toEntity(history);
        ProfileAssignmentHistoryEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ProfileAssignmentHistory> findOpenByProfileId(Long profileId) {
        return repository.findByProfileIdAndReleasedAtIsNull(profileId)
                .map(this::toDomain);
    }

    // ── Mapping helpers ──────────────────────────────────────────────────────

    private ProfileAssignmentHistoryEntity toEntity(ProfileAssignmentHistory domain) {
        return ProfileAssignmentHistoryEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .profileId(domain.getProfileId())
                .subscriptionId(domain.getSubscriptionId())
                .accountEmail(domain.getAccountEmail())
                .accountPassword(domain.getAccountPassword())
                .profileName(domain.getProfileName())
                .profilePin(domain.getProfilePin())
                .profileNotes(domain.getProfileNotes())
                .assignedAt(domain.getAssignedAt())
                .releasedAt(domain.getReleasedAt())
                .vendorId(domain.getVendorId())
                .build();
    }

    private ProfileAssignmentHistory toDomain(ProfileAssignmentHistoryEntity entity) {
        return ProfileAssignmentHistory.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .profileId(entity.getProfileId())
                .subscriptionId(entity.getSubscriptionId())
                .accountEmail(entity.getAccountEmail())
                .accountPassword(entity.getAccountPassword())
                .profileName(entity.getProfileName())
                .profilePin(entity.getProfilePin())
                .profileNotes(entity.getProfileNotes())
                .assignedAt(entity.getAssignedAt())
                .releasedAt(entity.getReleasedAt())
                .vendorId(entity.getVendorId())
                .build();
    }
}
