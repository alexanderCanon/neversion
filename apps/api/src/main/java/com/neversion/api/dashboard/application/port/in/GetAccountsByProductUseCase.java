package com.neversion.api.dashboard.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.dashboard.application.result.AccountGroupResult;

/**
 * Inbound port: get accounts for a product with profile availability.
 */
public interface GetAccountsByProductUseCase {
    List<AccountGroupResult> getByProductId(UUID productId);
}
