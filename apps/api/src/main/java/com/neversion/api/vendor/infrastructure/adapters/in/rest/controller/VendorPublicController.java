package com.neversion.api.vendor.infrastructure.adapters.in.rest.controller;

import com.neversion.api.exception.ResourceNotFoundException;
import com.neversion.api.vendor.domain.port.out.VendorRepositoryPort;
import com.neversion.api.vendor.infrastructure.adapters.in.rest.dto.VendorPublicResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Public REST controller exposing vendor identity for storefront multi-tenancy resolution.
 * <p>
 * No authentication required — the storefront uses this endpoint to discover
 * the vendor UUID and basic branding (name, logo) from a known identifier.
 */
@RestController
@RequestMapping(value = "/api/v1/vendors/public", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Vendors — Public", description = "Public vendor lookup for storefront multi-tenancy")
public class VendorPublicController {

    private final VendorRepositoryPort vendorRepositoryPort;

    public VendorPublicController(VendorRepositoryPort vendorRepositoryPort) {
        this.vendorRepositoryPort = vendorRepositoryPort;
    }

    /**
     * Returns basic public vendor information by UUID.
     * <p>
     * The storefront calls this endpoint on load to resolve the vendor identity
     * without needing a hardcoded UUID in the frontend build (ADR-02).
     *
     * @param uuid vendor's public UUID
     * @return vendor name, logo, uuid and discount configuration
     */
    @GetMapping("/{uuid}")
    @Operation(
            summary = "Get public vendor info by UUID",
            description = "Returns store name, logo and discount configuration for storefront initialization. No authentication required.")
    @ApiResponse(responseCode = "200", description = "Vendor found")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    public ResponseEntity<VendorPublicResponse> getByUuid(@PathVariable UUID uuid) {
        var vendor = vendorRepositoryPort.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + uuid));

        return ResponseEntity.ok(new VendorPublicResponse(
                vendor.getUuid(),
                vendor.getStoreName(),
                vendor.getLogoUrl(),
                vendor.getDiscountCfg(),
                vendor.getRewardsCfg()));
    }
}
