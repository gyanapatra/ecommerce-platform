package com.product.ecommerce.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderEvent(UUID eventId, String eventType, UUID orderId, UUID userId,
    String status, BigDecimal totalAmount, List<OrderLineEvent> items, Instant occurredAt) {
  public static final String ORDER_PLACED = "ORDER_PLACED";
  public static final String ORDER_CANCELLED = "ORDER_CANCELLED";
}
