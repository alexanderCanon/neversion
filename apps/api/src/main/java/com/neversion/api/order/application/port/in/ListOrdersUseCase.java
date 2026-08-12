package com.neversion.api.order.application.port.in;

import java.util.List;
import java.util.UUID;

import com.neversion.api.order.domain.model.Order;
import com.neversion.api.order.domain.model.enums.OrderStatus;

/**
 * UC5: List Orders — US-037.
 */
public interface ListOrdersUseCase {

    List<Order> listByVendor(UUID vendorUuid, UUID clientUuid, OrderStatus status, String callerExternalId);

    List<Order> listOrders(UUID clientUuid, OrderStatus status, String callerExternalId);
}

