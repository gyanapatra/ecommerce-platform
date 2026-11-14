package com.product.ecommerce.order.api;

import com.product.ecommerce.order.domain.CustomerOrderItem;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
    UUID productId, String productName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
  static OrderItemResponse from(CustomerOrderItem item) {
    return new OrderItemResponse(item.getProductId(), item.getProductName(), item.getQuantity(),
        item.getUnitPrice(), item.getLineTotal());
  }
}