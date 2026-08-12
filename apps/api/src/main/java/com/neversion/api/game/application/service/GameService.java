package com.neversion.api.game.application.service;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.game.application.port.in.GameUseCase;
import com.neversion.api.game.domain.model.Game;
import com.neversion.api.game.domain.port.out.GameRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for Game (parent) operations.
 * Enforces ownership validations and multi-tenancy rules.
 */
@Service
public class GameService implements GameUseCase {

    private final GameRepositoryPort gameRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;

    public GameService(
            GameRepositoryPort gameRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort) {
        this.gameRepositoryPort = gameRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    @Override
    @Transactional
    public Game create(Game game, String callerExternalId) {
        Long vendorId = resolveVendorId(callerExternalId);

        if (gameRepositoryPort.existsByVendorIdAndSlug(vendorId, game.getSlug())) {
            throw new BusinessRuleException(
                    "A game with slug '" + game.getSlug() + "' already exists for this vendor.");
        }

        Game toSave = Game.builder()
                .vendorId(vendorId)
                .name(game.getName())
                .slug(game.getSlug())
                .imageUrl(game.getImageUrl())
                .isActive(true)
                .build();

        return gameRepositoryPort.save(toSave);
    }

    @Override
    @Transactional
    public Game update(UUID uuid, Game updated, String callerExternalId) {
        Game existing = gameRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + uuid));

        assertOwnership(existing, callerExternalId);

        // Check slug uniqueness if slug changed
        if (!existing.getSlug().equalsIgnoreCase(updated.getSlug())) {
            if (gameRepositoryPort.existsByVendorIdAndSlug(existing.getVendorId(), updated.getSlug())) {
                throw new BusinessRuleException(
                        "A game with slug '" + updated.getSlug() + "' already exists for this vendor.");
            }
            existing.setSlug(updated.getSlug());
        }

        existing.setName(updated.getName());
        existing.setImageUrl(updated.getImageUrl());

        return gameRepositoryPort.save(existing);
    }

    @Override
    @Transactional
    public Game toggleStatus(UUID uuid, String callerExternalId) {
        Game existing = gameRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + uuid));

        assertOwnership(existing, callerExternalId);

        existing.setIsActive(!existing.getIsActive());
        return gameRepositoryPort.save(existing);
    }

    @Override
    public List<Game> listByVendor(UUID vendorUuid, Boolean isActive, String callerExternalId) {
        Vendor vendor = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid));

        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(vendor.getId())) {
            throw new AccessDeniedException("Access denied: you do not own vendor " + vendorUuid);
        }

        if (isActive != null) {
            return gameRepositoryPort.findAllByVendorId(vendor.getId()).stream()
                    .filter(g -> g.getIsActive().equals(isActive))
                    .toList();
        }

        return gameRepositoryPort.findAllByVendorId(vendor.getId());
    }

    @Override
    public List<Game> listByVendor(Boolean isActive, String callerExternalId) {
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (isActive != null) {
            return gameRepositoryPort.findAllByVendorId(callerVendorId).stream()
                    .filter(g -> g.getIsActive().equals(isActive))
                    .toList();
        }
        return gameRepositoryPort.findAllByVendorId(callerVendorId);
    }


    @Override
    public List<Game> listActive(UUID vendorUuid) {
        Long vendorId = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid))
                .getId();

        return gameRepositoryPort.findActiveByVendorId(vendorId);
    }

    @Override
    public Game getActiveBySlug(UUID vendorUuid, String slug) {
        Long vendorId = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid))
                .getId();

        Game game = gameRepositoryPort.findByVendorIdAndSlug(vendorId, slug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Game not found for slug: " + slug));

        if (!Boolean.TRUE.equals(game.getIsActive())) {
            throw new ResourceNotFoundException("Game not found for slug: " + slug);
        }

        return game;
    }

    @Override
    public Game getById(UUID uuid) {
        return gameRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + uuid));
    }

    @Override
    public List<Game> getAll() {
        return gameRepositoryPort.findAll();
    }

    @Override
    @Transactional
    public void delete(UUID uuid, String callerExternalId) {
        Game existing = gameRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Game not found: " + uuid));

        assertOwnership(existing, callerExternalId);

        // Logical delete (soft deactivation)
        existing.setIsActive(false);
        gameRepositoryPort.save(existing);
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

    private void assertOwnership(Game game, String callerExternalId) {
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(game.getVendorId())) {
            throw new AccessDeniedException("Access denied: you do not own game " + game.getUuid());
        }
    }
}
