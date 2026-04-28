package com.neversion.api.order.application.port.in;

import java.util.UUID;

import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.enums.OrderStatus;

/**
 * UC6: Change Order Status — US-038, US-039.
 */
public interface ChangeOrderStatusUseCase {

    Order changeStatus(UUID orderUuid, OrderStatus newStatus, String notes, String callerExternalId);
}
