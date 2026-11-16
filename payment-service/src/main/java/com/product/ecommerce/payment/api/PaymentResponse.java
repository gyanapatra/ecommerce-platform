package com.product.ecommerce.payment.api;

import com.product.ecommerce.payment.domain.Payment;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(UUID id, UUID orderId, BigDecimal amount, String currency,
    String status, String provider, String providerReference, Instant createdAt, Instant updatedAt) {
  public static PaymentResponse from(Payment payment) {
    return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getAmount(),
        payment.getCurrency(), payment.getStatus().name(), payment.getProvider(),
        payment.getProviderReference(), payment.getCreatedAt(), payment.getUpdatedAt());
  }
}