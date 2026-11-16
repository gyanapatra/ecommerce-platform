package com.product.ecommerce.order.domain;

public enum OrderStatus {
  PROCESSING,
  PLACED,
  PAID,
  CANCELLED,
  REJECTED,
  COMPENSATION_REQUIRED
}