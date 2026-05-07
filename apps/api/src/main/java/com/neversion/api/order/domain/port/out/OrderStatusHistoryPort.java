package com.neversion.api.order.domain.port.out;

import java.util.List;

import com.neversion.api.order.domain.model.OrderStatusChange;

/**
 * US-038 CA3: Port for recording and retrieving order status changes.
 */
public interface OrderStatusHistoryPort {

    OrderStatusChange record(OrderStatusChange change);

    List<OrderStatusChange> findByOrderId(Long orderId);
}
