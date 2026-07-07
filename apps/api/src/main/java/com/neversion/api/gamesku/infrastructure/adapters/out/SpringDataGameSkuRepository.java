package com.neversion.api.gamesku.infrastructure.adapters.out;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataGameSkuRepository extends JpaRepository<GameSkuEntity, Long> {

    Optional<GameSkuEntity> findByUuid(UUID uuid);

    Optional<GameSkuEntity> findByVendorIdAndCode(Long vendorId, String code);

    List<GameSkuEntity> findAllByVendorId(Long vendorId);

    List<GameSkuEntity> findAllByVendorIdAndIsActiveTrue(Long vendorId);

    List<GameSkuEntity> findByVendorIdAndGameId(Long vendorId, Long gameId);

    List<GameSkuEntity> findByVendorIdAndGameIdAndIsActiveTrue(Long vendorId, Long gameId);

    boolean existsByVendorIdAndCode(Long vendorId, String code);
}
