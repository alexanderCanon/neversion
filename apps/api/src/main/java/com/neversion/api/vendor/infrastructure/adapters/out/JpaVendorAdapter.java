package com.neversion.api.vendor.infrastructure.adapters.out;

import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import com.neversion.api.vendor.infrastructure.adapters.out.mapper.VendorPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter implementing VendorRepositoryPort.
 * VendorEntity stores user_id as a plain Long — no cross-module entity dependency.
 * The DB FK constraint (V8) guarantees referential integrity.
 */
@Component("vendorRepositoryAdapter")
public class JpaVendorAdapter implements VendorRepositoryPort {

    private final SpringDataVendorRepository vendorRepository;

    public JpaVendorAdapter(SpringDataVendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Override
    public Vendor save(Vendor vendor) {
        VendorEntity entity = VendorPersistenceMapper.toEntity(vendor);
        VendorEntity saved = vendorRepository.save(entity);
        return VendorPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Vendor> findByUuid(UUID uuid) {
        return vendorRepository.findByUuid(uuid)
                .map(VendorPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Vendor> findByUserId(Long userId) {
        return vendorRepository.findByUserId(userId)
                .map(VendorPersistenceMapper::toDomain);
    }

    /** US-033: Lookup by internal PK. */
    @Override
    public Optional<Vendor> findByInternalId(Long id) {
        return vendorRepository.findById(id)
                .map(VendorPersistenceMapper::toDomain);
    }

    @Override
    public List<Vendor> findAll() {
        return vendorRepository.findAll().stream()
                .map(VendorPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return vendorRepository.existsByUserId(userId);
    }

    @Override
    public void deleteByUuid(UUID uuid) {
        vendorRepository.deleteByUuid(uuid);
    }
}
