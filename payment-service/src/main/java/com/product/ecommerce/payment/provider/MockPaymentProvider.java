package com.product.ecommerce.payment.provider;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!real-payments")
public class MockPaymentProvider implements PaymentProvider {
  @Override
  public PaymentReceipt charge(UUID idempotencyKey, BigDecimal amount, String currency) {
    return new PaymentReceipt("mock-" + idempotencyKey);
  }
}