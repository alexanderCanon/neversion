package com.neversion.api.account.domain.model.enums;

/**
 * Determines how an Account's profiles are sold.
 * BY_PROFILE  – profiles are sold individually to different clients.
 * FULL_ACCOUNT – the entire account is assigned to a single client.
 */
public enum SaleMode {
    BY_PROFILE,
    FULL_ACCOUNT
}
