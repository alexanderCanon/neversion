package com.neversion.api.user.infrastructure.adapters.out;

import com.neversion.api.user.domain.model.User;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.user.infrastructure.adapters.out.mapper.UserPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA adapter implementing the UserRepositoryPort outbound port.
 * Translates between domain model and JPA entity using explicit mappers.
 */
@Component
public class JpaUserAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    public JpaUserAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserPersistenceMapper.toEntity(user);
        UserEntity saved = repository.save(entity);
        return UserPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByExternalId(String externalId) {
        return repository.findByExternalId(externalId)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByExternalId(String externalId) {
        return repository.existsByExternalId(externalId);
    }
}

