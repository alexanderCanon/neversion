package com.neversion.api.game.domain.port.out;

import com.neversion.api.game.domain.model.Game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepositoryPort {

    Game save(Game game);

    Optional<Game> findById(UUID uuid);

    Optional<Game> findByInternalId(Long id);

    Optional<Game> findByVendorIdAndSlug(Long vendorId, String slug);

    List<Game> findAllByVendorId(Long vendorId);

    List<Game> findActiveByVendorId(Long vendorId);

    List<Game> findAll();

    boolean existsByVendorIdAndSlug(Long vendorId, String slug);
}
