package com.neversion.api.game.infrastructure.adapters.out;

import com.neversion.api.game.domain.model.Game;
import com.neversion.api.game.domain.port.out.GameRepositoryPort;
import com.neversion.api.game.infrastructure.adapters.out.mapper.GamePersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaGameAdapter implements GameRepositoryPort {

    private final SpringDataGameRepository repository;
    private final GamePersistenceMapper mapper;

    public JpaGameAdapter(SpringDataGameRepository repository, GamePersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Game save(Game game) {
        GameEntity entity = mapper.toEntity(game);
        GameEntity saved = repository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Game> findById(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDomain);
    }

    @Override
    public Optional<Game> findByInternalId(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Game> findByVendorIdAndSlug(Long vendorId, String slug) {
        return repository.findByVendorIdAndSlug(vendorId, slug).map(mapper::toDomain);
    }

    @Override
    public List<Game> findAllByVendorId(Long vendorId) {
        return repository.findAllByVendorId(vendorId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Game> findActiveByVendorId(Long vendorId) {
        return repository.findAllByVendorIdAndIsActiveTrue(vendorId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Game> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByVendorIdAndSlug(Long vendorId, String slug) {
        return repository.existsByVendorIdAndSlug(vendorId, slug);
    }
}
