package com.product.ecommerce.order.exception;

public class OrderConflictException extends RuntimeException {
  public OrderConflictException(String message) {
    super(message);
  }
}