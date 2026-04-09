package com.neversion.api.dashboard.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.dashboard.application.result.ProfileResult;

/**
 * Inbound port: get profiles for an account with subscription data.
 */
public interface GetProfilesByAccountUseCase {
    List<ProfileResult> getByAccountId(UUID accountId);
}
