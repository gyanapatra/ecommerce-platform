package com.product.ecommerce.payment.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PaymentRequest(@NotNull UUID orderId, @NotNull PaymentMethod paymentMethod) {
  public enum PaymentMethod { MOCK }
}