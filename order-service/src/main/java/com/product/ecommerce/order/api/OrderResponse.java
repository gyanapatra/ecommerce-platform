package com.product.ecommerce.order.api;

import com.product.ecommerce.order.domain.CustomerOrder;
import com.product.ecommerce.order.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id, UUID userId, OrderStatus status, List<OrderItemResponse> items,
    BigDecimal totalAmount, Instant createdAt, Instant updatedAt) {
  public static OrderResponse from(CustomerOrder order) {
    return new OrderResponse(order.getId(), order.getUserId(), order.getStatus(),
        order.getItems().stream().map(OrderItemResponse::from).toList(),
        order.getTotalAmount(), order.getCreatedAt(), order.getUpdatedAt());
  }
}