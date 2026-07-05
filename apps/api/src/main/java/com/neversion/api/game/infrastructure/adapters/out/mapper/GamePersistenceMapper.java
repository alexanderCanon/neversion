package com.neversion.api.game.infrastructure.adapters.out.mapper;

import com.neversion.api.game.domain.model.Game;
import com.neversion.api.game.infrastructure.adapters.out.GameEntity;
import org.springframework.stereotype.Component;

@Component
public class GamePersistenceMapper {

    public Game toDomain(GameEntity entity) {
        if (entity == null) return null;
        return Game.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .vendorId(entity.getVendorId())
                .code(entity.getCode())
                .name(entity.getName())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public GameEntity toEntity(Game domain) {
        if (domain == null) return null;
        return GameEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .vendorId(domain.getVendorId())
                .code(domain.getCode())
                .name(domain.getName())
                .price(domain.getPrice())
                .imageUrl(domain.getImageUrl())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
