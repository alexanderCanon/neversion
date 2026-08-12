package com.neversion.api.service.application.service;

import com.neversion.api.exception.BusinessRuleException;
import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.service.application.port.in.ServiceUseCase;
import com.neversion.api.service.domain.port.out.ServiceRepositoryPort;
import com.neversion.api.shared.domain.model.enums.CategoryType;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service implementing the EPIC-02 use cases for the service catalog.
 * <p>
 * Ownership enforcement (US-018 / US-019):
 * The JWT subject (Supabase externalId) is resolved to an internal User,
 * then to a Vendor, and then compared to the service's vendorId.
 * SUPER_ADMIN bypasses this check (ADR-08).
 */
@Service
public class DigitalServiceService implements ServiceUseCase {

    private final ServiceRepositoryPort serviceRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;

    public DigitalServiceService(
            ServiceRepositoryPort serviceRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort) {
        this.serviceRepositoryPort = serviceRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    // ─── US-017: Create ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public com.neversion.api.service.domain.model.Service create(
            com.neversion.api.service.domain.model.Service service,
            String callerExternalId) {

        if (serviceRepositoryPort.existsByName(service.getName())) {
            throw new BusinessRuleException(
                    "A service named '" + service.getName() + "' already exists.");
        }

        // Resolve vendorId from the caller's external_id
        Long vendorId = resolveVendorId(callerExternalId);
        com.neversion.api.service.domain.model.Service toSave = com.neversion.api.service.domain.model.Service.builder()
                .name(service.getName())
                .category(service.getCategory())
                .priceProfile(service.getPriceProfile())
                .priceFull(service.getPriceFull())
                .durationDays(service.getDurationDays())
                .maxProfiles(service.getMaxProfiles())
                .description(service.getDescription())
                .imageUrl(service.getImageUrl())
                .details(service.getDetails())
                .vendorId(vendorId)
                .isActive(true)
                .build();

        return serviceRepositoryPort.save(toSave);
    }

    // ─── US-018: Update ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public com.neversion.api.service.domain.model.Service update(UUID uuid,
            com.neversion.api.service.domain.model.Service updated,
            String callerExternalId) {

        com.neversion.api.service.domain.model.Service existing =
                serviceRepositoryPort.findById(uuid)
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + uuid));

        assertOwnership(existing, callerExternalId);

        // All editable fields — identifiers (id, uuid) are never changed (US-018 BR)
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setPriceProfile(updated.getPriceProfile());
        existing.setPriceFull(updated.getPriceFull());
        existing.setDurationDays(updated.getDurationDays());
        existing.setMaxProfiles(updated.getMaxProfiles());
        existing.setDescription(updated.getDescription());
        existing.setImageUrl(updated.getImageUrl());
        existing.setDetails(updated.getDetails());

        return serviceRepositoryPort.save(existing);
    }

    // ─── US-019: Toggle status ───────────────────────────────────────────────

    @Override
    @Transactional
    public com.neversion.api.service.domain.model.Service toggleStatus(UUID uuid, String callerExternalId) {
        com.neversion.api.service.domain.model.Service existing =
                serviceRepositoryPort.findById(uuid)
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + uuid));

        assertOwnership(existing, callerExternalId);

        // Toggle: does not affect active subscriptions (US-019 BR)
        existing.setIsActive(!existing.getIsActive());
        return serviceRepositoryPort.save(existing);
    }

    // ─── US-020: Vendor panel list (all + filters) ───────────────────────────

    @Override
    public List<com.neversion.api.service.domain.model.Service> listByVendor(
            UUID vendorUuid, CategoryType category, Boolean isActive, String callerExternalId) {

        Vendor vendor = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid));
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(vendor.getId())) {
            throw new AccessDeniedException("Access denied: you do not own vendor " + vendorUuid);
        }

        if (category == null && isActive == null) {
            return serviceRepositoryPort.findAllByVendorId(vendor.getId());
        }
        return serviceRepositoryPort.findByVendorIdAndFilters(vendor.getId(), category, isActive);
    }

    @Override
    public List<com.neversion.api.service.domain.model.Service> listByVendor(
            CategoryType category, Boolean isActive, String callerExternalId) {
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (category == null && isActive == null) {
            return serviceRepositoryPort.findAllByVendorId(callerVendorId);
        }
        return serviceRepositoryPort.findByVendorIdAndFilters(callerVendorId, category, isActive);
    }


    // ─── US-021: Public store catalog (active only) ──────────────────────────

    @Override
    public List<com.neversion.api.service.domain.model.Service> listActive(UUID vendorUuid) {
        Long vendorId = vendorRepositoryPort.findByUuid(vendorUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + vendorUuid))
                .getId();
        return serviceRepositoryPort.findActiveByVendorId(vendorId);
    }

    // ─── Legacy / admin operations ───────────────────────────────────────────

    @Override
    public com.neversion.api.service.domain.model.Service getById(UUID uuid) {
        return serviceRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + uuid));
    }

    @Override
    public List<com.neversion.api.service.domain.model.Service> getAll() {
        return serviceRepositoryPort.findAll();
    }

    @Override
    @Transactional
    public void delete(UUID uuid) {
        serviceRepositoryPort.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + uuid));
        serviceRepositoryPort.deleteById(uuid);
    }

    // ─── Ownership helpers ───────────────────────────────────────────────────

    /**
     * Resolves the Supabase externalId → User → Vendor internal id.
     * Throws ResourceNotFoundException if the chain is broken.
     */
    private Long resolveVendorId(String callerExternalId) {
        var user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));
        return vendorRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor record not found for user: " + user.getExternalId()))
                .getId();
    }

    /**
     * Verifies the caller owns the service.
     * Throws AccessDeniedException (→ 403) if the vendorId does not match.
     */
    private void assertOwnership(com.neversion.api.service.domain.model.Service service,
            String callerExternalId) {
        Long callerVendorId = resolveVendorId(callerExternalId);
        if (!callerVendorId.equals(service.getVendorId())) {
            throw new AccessDeniedException("Access denied: you do not own service " + service.getUuid());
        }
    }
}
