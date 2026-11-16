package com.product.ecommerce.payment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentTest {
  @Test
  void paymentCanOnlyMoveToSucceededOnce() {
    Payment payment = new Payment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        new BigDecimal("12.50"), "INR");

    payment.markSucceeded("mock-first");
    payment.markSucceeded("mock-retry");

    assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
    assertEquals("mock-first", payment.getProviderReference());
    assertEquals(new BigDecimal("12.50"), payment.getAmount());
  }
}
