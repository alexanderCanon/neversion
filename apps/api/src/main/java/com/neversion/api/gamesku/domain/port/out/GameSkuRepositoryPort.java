package com.neversion.api.gamesku.domain.port.out;

import com.neversion.api.gamesku.domain.model.GameSku;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameSkuRepositoryPort {

    GameSku save(GameSku gameSku);

    Optional<GameSku> findById(UUID uuid);

    Optional<GameSku> findByInternalId(Long id);

    Optional<GameSku> findByVendorIdAndCode(Long vendorId, String code);

    List<GameSku> findAllByVendorId(Long vendorId);

    List<GameSku> findActiveByVendorId(Long vendorId);

    List<GameSku> findByVendorIdAndGameId(Long vendorId, Long gameId);

    List<GameSku> findActiveByVendorIdAndGameId(Long vendorId, Long gameId);

    List<GameSku> findAll();

    boolean existsByVendorIdAndCode(Long vendorId, String code);
}
