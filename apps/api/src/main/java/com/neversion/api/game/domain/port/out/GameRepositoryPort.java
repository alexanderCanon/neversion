package com.neversion.api.game.domain.port.out;

import com.neversion.api.game.domain.model.Game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepositoryPort {

    Game save(Game game);

    Optional<Game> findById(UUID uuid);

    Optional<Game> findByInternalId(Long id);

    Optional<Game> findByVendorIdAndCode(Long vendorId, String code);

    List<Game> findAllByVendorId(Long vendorId);

    List<Game> findActiveByVendorId(Long vendorId);

    List<Game> findAll();

    boolean existsByVendorIdAndCode(Long vendorId, String code);
}
