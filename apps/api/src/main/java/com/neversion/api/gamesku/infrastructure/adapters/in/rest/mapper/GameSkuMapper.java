package com.neversion.api.gamesku.infrastructure.adapters.in.rest.mapper;

import com.neversion.api.game.domain.model.Game;
import com.neversion.api.game.domain.port.out.GameRepositoryPort;
import com.neversion.api.gamesku.domain.model.GameSku;
import com.neversion.api.gamesku.infrastructure.adapters.in.rest.dto.GameSkuRequest;
import com.neversion.api.gamesku.infrastructure.adapters.in.rest.dto.GameSkuResponse;
import org.springframework.stereotype.Component;

@Component
public class GameSkuMapper {

    private final GameRepositoryPort gameRepositoryPort;

    public GameSkuMapper(GameRepositoryPort gameRepositoryPort) {
        this.gameRepositoryPort = gameRepositoryPort;
    }

    public GameSku toDomain(GameSkuRequest request) {
        if (request == null) return null;
        return GameSku.builder()
                .code(request.code())
                .name(request.name())
                .price(request.price())
                .imageUrl(request.imageUrl())
                .gameUuid(request.gameUuid())
                .build();
    }

    public GameSkuResponse toResponse(GameSku gameSku) {
        if (gameSku == null) return null;

        Game game = gameSku.getGameId() != null
                ? gameRepositoryPort.findByInternalId(gameSku.getGameId()).orElse(null)
                : null;

        return GameSkuResponse.builder()
                .id(gameSku.getUuid())
                .code(gameSku.getCode())
                .name(gameSku.getName())
                .price(gameSku.getPrice())
                .imageUrl(gameSku.getImageUrl())
                .isActive(gameSku.getIsActive())
                .gameUuid(game != null ? game.getUuid() : gameSku.getGameUuid())
                .gameSlug(game != null ? game.getSlug() : null)
                .gameName(game != null ? game.getName() : null)
                .createdAt(gameSku.getCreatedAt())
                .build();
    }
}
