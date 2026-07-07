package com.neversion.api.gamesku.infrastructure.adapters.out;

import com.neversion.api.gamesku.domain.model.GameSku;
import com.neversion.api.gamesku.domain.port.out.GameSkuRepositoryPort;
import com.neversion.api.gamesku.infrastructure.adapters.out.mapper.GameSkuPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaGameSkuAdapter implements GameSkuRepositoryPort {

    private final SpringDataGameSkuRepository repository;
    private final GameSkuPersistenceMapper mapper;

    public JpaGameSkuAdapter(SpringDataGameSkuRepository repository, GameSkuPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public GameSku save(GameSku gameSku) {
        GameSkuEntity entity = mapper.toEntity(gameSku);
        GameSkuEntity saved = repository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<GameSku> findById(UUID uuid) {
        return repository.findByUuid(uuid).map(mapper::toDomain);
    }

    @Override
    public Optional<GameSku> findByInternalId(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<GameSku> findByVendorIdAndCode(Long vendorId, String code) {
        return repository.findByVendorIdAndCode(vendorId, code).map(mapper::toDomain);
    }

    @Override
    public List<GameSku> findAllByVendorId(Long vendorId) {
        return repository.findAllByVendorId(vendorId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<GameSku> findActiveByVendorId(Long vendorId) {
        return repository.findAllByVendorIdAndIsActiveTrue(vendorId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<GameSku> findByVendorIdAndGameId(Long vendorId, Long gameId) {
        return repository.findByVendorIdAndGameId(vendorId, gameId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<GameSku> findActiveByVendorIdAndGameId(Long vendorId, Long gameId) {
        return repository.findByVendorIdAndGameIdAndIsActiveTrue(vendorId, gameId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<GameSku> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByVendorIdAndCode(Long vendorId, String code) {
        return repository.existsByVendorIdAndCode(vendorId, code);
    }
}
