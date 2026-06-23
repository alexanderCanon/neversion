package com.neversion.api.shared.domain.model.enums;

/**
 * Client's preference at Spotify Family checkout.
 * Only relevant when Account.profileDeliveryType == ProfileDeliveryType.PERSONAL_ACCOUNT.
 *
 * CUENTA_NUEVA  — vendor creates a fresh Spotify account; credentials delivered by email.
 * CUENTA_PROPIA — client already has a Spotify account; vendor adds them via WhatsApp.
 */
public enum AccountPreference {
    CUENTA_NUEVA,
    CUENTA_PROPIA
}
