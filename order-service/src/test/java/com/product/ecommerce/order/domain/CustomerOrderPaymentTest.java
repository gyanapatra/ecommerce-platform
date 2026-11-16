package com.product.ecommerce.order.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CustomerOrderPaymentTest {
  @Test
  void paidOrderIsIdempotentAndCannotBeCancelled() {
    CustomerOrder order = new CustomerOrder(UUID.randomUUID(), UUID.randomUUID());
    order.addItem(UUID.randomUUID(), "Sample", 1, new BigDecimal("12.50"));
    order.markPlaced();

    order.markPaid();
    order.markPaid();

    assertEquals(OrderStatus.PAID, order.getStatus());
    assertThrows(IllegalStateException.class, order::markCancelled);
  }
}
