package com.neversion.api.service.application.port.in;

import com.neversion.api.service.domain.model.Service;
import com.neversion.api.shared.domain.model.enums.CategoryType;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for the digital service catalog use cases (EPIC-02).
 * Implemented by DigitalServiceService.
 */
public interface ServiceUseCase {

    /** US-017: Create a new service in the vendor's catalog. */
    Service create(Service service, String callerExternalId);

    /** US-018: Update all editable fields of an existing service. */
    Service update(UUID uuid, Service updated, String callerExternalId);

    /** US-019: Toggle the active/inactive status of a service. */
    Service toggleStatus(UUID uuid, String callerExternalId);

    /** US-020: List all services for the vendor panel (active + inactive, with optional filters). */
    List<Service> listByVendor(UUID vendorUuid, CategoryType category, Boolean isActive, String callerExternalId);

    List<Service> listByVendor(CategoryType category, Boolean isActive, String callerExternalId);


    /** US-021: List only active services for a vendor's public store. */
    List<Service> listActive(UUID vendorUuid);

    /** Single service lookup by public UUID. */
    Service getById(UUID uuid);

    /** Super admin full list. */
    List<Service> getAll();

    /** Delete a service by public UUID. */
    void delete(UUID uuid);
}
