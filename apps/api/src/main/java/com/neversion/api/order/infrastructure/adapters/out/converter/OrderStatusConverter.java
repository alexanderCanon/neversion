package com.neversion.api.order.infrastructure.adapters.out.converter;

import com.neversion.api.infrastructure.EnumConverter;
import com.neversion.api.order.domain.model.enums.OrderStatus;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusConverter extends EnumConverter<OrderStatus> {

    public OrderStatusConverter() {
        super(OrderStatus.class);
    }
}
