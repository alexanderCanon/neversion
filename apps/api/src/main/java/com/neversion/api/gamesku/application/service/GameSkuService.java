package com.neversion.api.gamesku.application.service;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.game.domain.model.Game;
import com.neversion.api.game.domain.port.out.GameRepositoryPort;
import com.neversion.api.gamesku.application.port.in.GameSkuUseCase;
import com.neversion.api.gamesku.domain.model.GameSku;
import com.neversion.api.gamesku.domain.port.out.GameSkuRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for GameSku (child) operations.
 * Resolves gameUuid/gameSlug to the internal gameId via GameRepositoryPort
 * (same pattern as ListAccountsService resolves serviceUuid to serviceId).
 * Enforces ownership validations and multi-tenancy rules.
 */
@Service
public class GameSkuService implements GameSkuUseCase {

    private final GameSkuRepositoryPort gameSkuRepositoryPort;
    private final GameRepositoryPort gameRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;

    public GameSkuService(
            GameSkuRepositoryPort gameSkuRepositoryPort,
            GameRepositoryPort gameRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort) {
        this.gameSkuRepositoryPort = gameSkuRepositoryPort;
        this.gameRepositoryPort = gameRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    @Override
    @Transactional
    public GameSku create(GameSku gameSku, String callerExternalId) {
        Long vendorId = resolveVendorId(callerExternalId);

        if (gameSkuRepositoryPort.existsByVendorIdAndCode(vendorId, gameSku.getCode())) {
            throw new BusinessRuleException(
                    "A game SKU with code '" + gameSku.getCode() + "' already exists for this vendor.");
        }

        Long gameId = resolveGameId(vendorId, gameSku.getGameUuid());

        GameSku toSave = GameSku.builder()
                .vendorId(vendorId)
                .gameId(gameId)
                .code(gameSku.getCode())
                .name(gameSku.getName())
                .price(gameSku.getPrice())
                .imageUrl(gameSku.getImageUrl())
                .isActive(true)
                .build();

        return gameSkuRepositoryPort.save(toSave);
    }

    @Override
    @Transactional
    public GameSku update(UUID uuid, GameSku updated, String callerExternalId) {
        GameSku existing = gameSkuRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Game SKU not found: " + uuid));

        assertOwnership(existing, callerExternalId);

        // Check code uniqueness if code changed
        if (!existing.getCode().equalsIgnoreCase(updated.getCode())) {
            if (gameSkuRepositoryPort.existsByVendorIdAndCode(existing.getVendorId(), updated.getCode())) {
                throw new BusinessRuleException(
                        "A game SKU with code '" + updated.getCode() + "' already exists for this vendor.");
            }
            existing.setCode(updated.getCode());
        }

        // Resolve gameId if gameUuid changed
        if (updated.getGameUuid() != null) {
            Long gameId = resolveGameId(existing.getVendorId(), updated.getGameUuid());
            existing.setGameId(gameId);
        }

        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        existing.setImageUrl(updated.getImageUrl());

        return gameSkuRepositoryPort.save(existing);
    }

    @Override
    @Transactional
    public GameSku toggleStatus(UUID uuid, String callerExternalId) {
        GameSku existing = gameSkuRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Game SKU not found: " + uuid));

        assertOwnership(existing, callerExternalId);

        existing.setIsActive(!existing.getIsActive());
        return gameSkuRepositoryPort.save(existing);
    }

    @Override
    public List<GameSku> listByVendor(UUID vendorUuid, UUID gameUuid, Boolean isActive, String callerExternalId) {
        Vendor vendor = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid));

        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(vendor.getId())) {
            throw new AccessDeniedException("Access denied: you do not own vendor " + vendorUuid);
        }

        // Resolve optional gameUuid -> internal gameId
        Long gameId = null;
        if (gameUuid != null) {
            gameId = gameRepositoryPort.findById(gameUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameUuid))
                    .getId();
        }

        List<GameSku> skus;
        if (gameId != null) {
            skus = gameSkuRepositoryPort.findByVendorIdAndGameId(vendor.getId(), gameId);
        } else {
            skus = gameSkuRepositoryPort.findAllByVendorId(vendor.getId());
        }

        if (isActive != null) {
            return skus.stream()
                    .filter(s -> s.getIsActive().equals(isActive))
                    .toList();
        }

        return skus;
    }

    @Override
    public List<GameSku> listByVendor(UUID gameUuid, Boolean isActive, String callerExternalId) {
        Long callerVendorId = resolveVendorId(callerExternalId);

        Long gameId = null;
        if (gameUuid != null) {
            gameId = gameRepositoryPort.findById(gameUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameUuid))
                    .getId();
        }

        List<GameSku> skus;
        if (gameId != null) {
            skus = gameSkuRepositoryPort.findByVendorIdAndGameId(callerVendorId, gameId);
        } else {
            skus = gameSkuRepositoryPort.findAllByVendorId(callerVendorId);
        }

        if (isActive != null) {
            return skus.stream()
                    .filter(s -> s.getIsActive().equals(isActive))
                    .toList();
        }

        return skus;
    }


    @Override
    public List<GameSku> listActiveByGameSlug(UUID vendorUuid, String gameSlug) {
        Long vendorId = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid))
                .getId();

        Game game = gameRepositoryPort.findByVendorIdAndSlug(vendorId, gameSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Game not found for slug: " + gameSlug));

        if (!Boolean.TRUE.equals(game.getIsActive())) {
            throw new ResourceNotFoundException("Game not found for slug: " + gameSlug);
        }

        return gameSkuRepositoryPort.findActiveByVendorIdAndGameId(vendorId, game.getId());
    }

    @Override
    public GameSku getById(UUID uuid) {
        return gameSkuRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Game SKU not found: " + uuid));
    }

    @Override
    public List<GameSku> getAll() {
        return gameSkuRepositoryPort.findAll();
    }

    @Override
    @Transactional
    public void delete(UUID uuid, String callerExternalId) {
        GameSku existing = gameSkuRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Game SKU not found: " + uuid));

        assertOwnership(existing, callerExternalId);

        // Logical delete (soft deactivation)
        existing.setIsActive(false);
        gameSkuRepositoryPort.save(existing);
    }

    private Long resolveVendorId(String callerExternalId) {
        var user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));
        return vendorRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor record not found for user: " + user.getExternalId()))
                .getId();
    }

    private Long resolveGameId(Long vendorId, UUID gameUuid) {
        if (gameUuid == null) {
            return null;
        }
        Game game = gameRepositoryPort.findById(gameUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + gameUuid));
        if (!vendorId.equals(game.getVendorId())) {
            throw new AccessDeniedException("Access denied: game " + gameUuid + " belongs to another vendor");
        }
        return game.getId();
    }

    private void assertOwnership(GameSku gameSku, String callerExternalId) {
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(gameSku.getVendorId())) {
            throw new AccessDeniedException("Access denied: you do not own game SKU " + gameSku.getUuid());
        }
    }
}
