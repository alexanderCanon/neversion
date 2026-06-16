package com.neversion.api.shared.application.service;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.vendor.domain.model.Vendor;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import com.neversion.api.user.domain.port.out.UserRepositoryPort;

/**
 * Tech-debt remediation A2: single source of truth for tenant ownership
 * resolution and enforcement (ADR-02 / ADR-09).
 * <p>
 * Replaces the {@code resolveVendorId()} method that was duplicated verbatim
 * across ~17 application services. Centralizing the Supabase
 * {@code externalId → User → Vendor} resolution and the ownership assertion
 * makes it structurally harder to forget a cross-tenant guard when adding new
 * endpoints.
 */
@Service
public class VendorSecurityService {

    private final UserRepositoryPort userRepositoryPort;
    private final VendorRepositoryPort vendorRepositoryPort;

    public VendorSecurityService(UserRepositoryPort userRepositoryPort,
            VendorRepositoryPort vendorRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    /**
     * Resolves the authenticated caller's internal vendor id from the Supabase
     * subject claim.
     *
     * @param callerExternalId Supabase subject (JWT {@code sub}) of the caller
     * @return the caller's internal vendor id
     * @throws ResourceNotFoundException if the user or its vendor cannot be found
     */
    public Long resolveVendorId(String callerExternalId) {
        return resolveCallerVendor(callerExternalId).getId();
    }

    /**
     * Resolves the authenticated caller's full {@link Vendor} aggregate from the
     * Supabase subject claim.
     *
     * @param callerExternalId Supabase subject (JWT {@code sub}) of the caller
     * @return the caller's vendor
     * @throws ResourceNotFoundException if the user or its vendor cannot be found
     */
    public Vendor resolveCallerVendor(String callerExternalId) {
        var user = userRepositoryPort.findByExternalId(callerExternalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found for externalId: " + callerExternalId));
        return vendorRepositoryPort.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found for userId: " + user.getId()));
    }

    /**
     * Enforces ADR-02: the authenticated caller may only operate on resources
     * owned by its own vendor.
     *
     * @param callerVendorId      the caller's resolved vendor id
     * @param resourceVendorId    the vendor id the resource belongs to
     * @param resourceDescription human-readable resource description for the error message
     * @throws AccessDeniedException if the caller does not own the resource
     */
    public void assertOwnership(Long callerVendorId, Long resourceVendorId, String resourceDescription) {
        if (!Objects.equals(callerVendorId, resourceVendorId)) {
            throw new AccessDeniedException(
                    "Access denied: you do not own " + resourceDescription);
        }
    }
}
