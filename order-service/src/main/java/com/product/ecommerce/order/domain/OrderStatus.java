package com.product.ecommerce.order.domain;

public enum OrderStatus {
  PROCESSING,
  PLACED,
  PAYMENT_PENDING,
  PAID,
  CANCELLED,
  REJECTED,
  COMPENSATION_REQUIRED
}
