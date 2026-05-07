package com.neversion.api.user.domain.model.enums;

/**
 * Platform roles for internal user registry.
 * Values persisted in lowercase per NFR-06 and ADR-08.
 * BR: Access control is enforced by the backend based on this field.
 */
public enum UserRole {
    SUPER_ADMIN,
    VENDOR,
    CLIENT
}
