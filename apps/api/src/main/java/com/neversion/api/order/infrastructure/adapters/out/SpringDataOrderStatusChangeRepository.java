package com.neversion.api.order.infrastructure.adapters.out;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataOrderStatusChangeRepository extends JpaRepository<OrderStatusChangeEntity, Long> {

    List<OrderStatusChangeEntity> findByOrderIdOrderByChangedAtAsc(Long orderId);
}
