package com.neversion.api.exception;

/**
 * Thrown when attempting to assign an individual account that already has
 * an active subscription (BR-06: Individual Account Exclusivity).
 * Results in HTTP 409 Conflict.
 */
public class AccountOverbookingException extends BusinessRuleException {
    public AccountOverbookingException(String message) {
        super(message);
    }
}
