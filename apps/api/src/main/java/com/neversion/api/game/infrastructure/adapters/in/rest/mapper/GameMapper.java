package com.neversion.api.game.infrastructure.adapters.in.rest.mapper;

import com.neversion.api.game.domain.model.Game;
import com.neversion.api.game.infrastructure.adapters.in.rest.dto.GameRequest;
import com.neversion.api.game.infrastructure.adapters.in.rest.dto.GameResponse;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {

    public Game toDomain(GameRequest request) {
        if (request == null) return null;
        return Game.builder()
                .code(request.code())
                .name(request.name())
                .price(request.price())
                .imageUrl(request.imageUrl())
                .build();
    }

    public GameResponse toResponse(Game game) {
        if (game == null) return null;
        return GameResponse.builder()
                .id(game.getUuid())
                .code(game.getCode())
                .name(game.getName())
                .price(game.getPrice())
                .imageUrl(game.getImageUrl())
                .isActive(game.getIsActive())
                .createdAt(game.getCreatedAt())
                .build();
    }
}
