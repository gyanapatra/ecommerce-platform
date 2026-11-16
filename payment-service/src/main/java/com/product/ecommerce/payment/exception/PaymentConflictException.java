package com.product.ecommerce.payment.exception;

public class PaymentConflictException extends RuntimeException {
  public PaymentConflictException(String message) { super(message); }
}