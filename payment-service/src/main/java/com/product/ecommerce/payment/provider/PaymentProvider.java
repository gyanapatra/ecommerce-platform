package com.product.ecommerce.payment.provider;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProvider {
  PaymentReceipt charge(UUID idempotencyKey, BigDecimal amount, String currency);
}