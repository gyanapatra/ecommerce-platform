package com.product.ecommerce.payment.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MockPaymentProviderTest {
  @Test
  void usesStableReferenceForIdempotentRetry() {
    MockPaymentProvider provider = new MockPaymentProvider();
    UUID key = UUID.randomUUID();
    PaymentReceipt first = provider.charge(key, new BigDecimal("12.50"), "INR");
    PaymentReceipt retry = provider.charge(key, new BigDecimal("12.50"), "INR");
    assertEquals(first.reference(), retry.reference());
  }
}