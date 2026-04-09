package com.neversion.api.dashboard.application.port.in;

import java.util.List;

import com.neversion.api.dashboard.application.result.ProductSummaryResult;
import com.neversion.api.shared.domain.model.enums.CategoryType;

/**
 * Inbound port: get product summaries filtered by category.
 */
public interface GetProductsSummaryUseCase {
    List<ProductSummaryResult> getByCategory(CategoryType category);
}
