package com.neversion.api.gamesku.infrastructure.adapters.out.mapper;

import com.neversion.api.gamesku.domain.model.GameSku;
import com.neversion.api.gamesku.infrastructure.adapters.out.GameSkuEntity;
import org.springframework.stereotype.Component;

@Component
public class GameSkuPersistenceMapper {

    public GameSku toDomain(GameSkuEntity entity) {
        if (entity == null) return null;
        return GameSku.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .vendorId(entity.getVendorId())
                .gameId(entity.getGameId())
                .code(entity.getCode())
                .name(entity.getName())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public GameSkuEntity toEntity(GameSku domain) {
        if (domain == null) return null;
        return GameSkuEntity.builder()
                .id(domain.getId())
                .uuid(domain.getUuid())
                .vendorId(domain.getVendorId())
                .gameId(domain.getGameId())
                .code(domain.getCode())
                .name(domain.getName())
                .price(domain.getPrice())
                .imageUrl(domain.getImageUrl())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
