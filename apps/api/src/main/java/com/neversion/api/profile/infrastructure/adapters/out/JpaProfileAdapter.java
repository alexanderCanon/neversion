package com.neversion.api.profile.infrastructure.adapters.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.neversion.api.profile.domain.model.Profile;
import com.neversion.api.profile.domain.port.out.ProfileRepositoryPort;
import com.neversion.api.profile.infrastructure.adapters.out.mapper.ProfilePersistenceMapper;

@Repository
public class JpaProfileAdapter implements ProfileRepositoryPort {

    private final SpringDataProfileRepository profileRepo;
    private final ProfilePersistenceMapper profileMapper;

    public JpaProfileAdapter(SpringDataProfileRepository profileRepo,
            ProfilePersistenceMapper profileMapper) {
        this.profileRepo = profileRepo;
        this.profileMapper = profileMapper;
    }

    @Override
    public Profile save(Profile profile) {
        ProfileEntity entity = profileMapper.toEntity(profile);
        ProfileEntity saved = profileRepo.saveAndFlush(entity);
        return profileMapper.toDomain(saved);
    }

    @Override
    public Optional<Profile> findById(UUID uuid) {
        return profileRepo.findByUuid(uuid).map(profileMapper::toDomain);
    }

    @Override
    public Optional<Profile> findByInternalId(Long id) {
        return profileRepo.findById(id).map(profileMapper::toDomain);
    }

    @Override
    public List<Profile> findByAccountId(Long accountId) {
        return profileRepo.findByAccountId(accountId).stream()
                .map(profileMapper::toDomain)
                .toList();
    }

    @Override
    public List<Profile> findAvailableByAccountId(Long accountId) {
        return profileRepo.findAvailableByAccountId(accountId).stream()
                .map(profileMapper::toDomain)
                .toList();
    }

    @Override
    public void saveAll(List<Profile> profiles) {
        List<ProfileEntity> entities = profiles.stream()
                .map(profileMapper::toEntity)
                .toList();
        profileRepo.saveAll(entities);
    }

    @Override
    public void deleteById(UUID uuid) {
        profileRepo.findByUuid(uuid).ifPresent(e -> profileRepo.deleteById(e.getId()));
    }
}
