package com.neversion.api.profile.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.neversion.api.profile.application.port.in.ProfileUseCase;
import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.exception.ResourceNotFoundException;

@Service
public class ProfileService implements ProfileUseCase {

    private final ProfileRepositoryPort profileRepositoryPort;

    public ProfileService(ProfileRepositoryPort profileRepositoryPort) {
        this.profileRepositoryPort = profileRepositoryPort;
    }

    @Override
    @Transactional
    public Profile save(Profile profile) {
        return profileRepositoryPort.save(profile);
    }

    @Override
    public Optional<Profile> findById(UUID uuid) {
        return profileRepositoryPort.findById(uuid);
    }

    @Override
    public List<Profile> findByAccountId(Long accountId) {
        return profileRepositoryPort.findByAccountId(accountId);
    }

    @Override
    public List<Profile> findAvailableByAccountId(Long accountId) {
        return profileRepositoryPort.findAvailableByAccountId(accountId);
    }

    /**
     * Auto-generates N blank profiles when an Account is created (BR-01).
     * Profile names default to "Perfil N" — Admin can rename later.
     */
    @Override
    @Transactional
    public void generateProfilesForAccount(Long accountId, int count) {
        List<Profile> profiles = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            profiles.add(Profile.builder()
                    .accountId(accountId)
                    .name("Perfil " + i)
                    .isOwner(i == 1)   // first profile is the owner by default
                    .build());
        }
        profileRepositoryPort.saveAll(profiles);
    }

    @Override
    @Transactional
    public void deleteById(UUID uuid) {
        profileRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found: " + uuid));
        profileRepositoryPort.deleteById(uuid);
    }
}
