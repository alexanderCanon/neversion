package com.neversion.api.game.infrastructure.adapters.out;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataGameRepository extends JpaRepository<GameEntity, Long> {

    Optional<GameEntity> findByUuid(UUID uuid);

    Optional<GameEntity> findByVendorIdAndCode(Long vendorId, String code);

    List<GameEntity> findAllByVendorId(Long vendorId);

    List<GameEntity> findAllByVendorIdAndIsActiveTrue(Long vendorId);

    boolean existsByVendorIdAndCode(Long vendorId, String code);
}
