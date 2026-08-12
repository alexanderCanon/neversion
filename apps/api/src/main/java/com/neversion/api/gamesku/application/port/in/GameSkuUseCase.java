package com.neversion.api.gamesku.application.port.in;

import com.neversion.api.gamesku.domain.model.GameSku;

import java.util.List;
import java.util.UUID;

public interface GameSkuUseCase {

    GameSku create(GameSku gameSku, String callerExternalId);

    GameSku update(UUID uuid, GameSku gameSku, String callerExternalId);

    GameSku toggleStatus(UUID uuid, String callerExternalId);

    List<GameSku> listByVendor(UUID vendorUuid, UUID gameUuid, Boolean isActive, String callerExternalId);

    List<GameSku> listByVendor(UUID gameUuid, Boolean isActive, String callerExternalId);


    List<GameSku> listActiveByGameSlug(UUID vendorUuid, String gameSlug);

    GameSku getById(UUID uuid);

    List<GameSku> getAll();

    void delete(UUID uuid, String callerExternalId);
}
