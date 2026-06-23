package com.neversion.api.account.domain.model.enums;

/**
 * Defines how access credentials are delivered to the client for a BY_PROFILE account.
 *
 * PROFILE_SLOT     — client receives a shared profile slot (name + PIN). E.g. Netflix.
 * PERSONAL_ACCOUNT — client receives or brings their own account linked to a family plan. E.g. Spotify Family.
 *
 * Only meaningful when Account.saleMode == SaleMode.BY_PROFILE.
 * For FULL_ACCOUNT sales mode, this field is irrelevant and should be null.
 */
public enum ProfileDeliveryType {
    PROFILE_SLOT,
    PERSONAL_ACCOUNT
}
