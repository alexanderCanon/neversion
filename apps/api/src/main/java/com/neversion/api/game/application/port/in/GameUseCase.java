package com.neversion.api.game.application.port.in;

import com.neversion.api.game.domain.model.Game;

import java.util.List;
import java.util.UUID;

public interface GameUseCase {

    Game create(Game game, String callerExternalId);

    Game update(UUID uuid, Game game, String callerExternalId);

    Game toggleStatus(UUID uuid, String callerExternalId);

    List<Game> listByVendor(UUID vendorUuid, Boolean isActive, String callerExternalId);

    List<Game> listByVendor(Boolean isActive, String callerExternalId);


    List<Game> listActive(UUID vendorUuid);

    Game getActiveBySlug(UUID vendorUuid, String slug);

    Game getById(UUID uuid);

    List<Game> getAll();

    void delete(UUID uuid, String callerExternalId);
}
