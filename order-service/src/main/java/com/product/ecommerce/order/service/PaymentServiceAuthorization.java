package com.product.ecommerce.order.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentServiceAuthorization {
  private final String configuredKey;

  public PaymentServiceAuthorization(@Value("${app.payment-service-key:}") String configuredKey) {
    this.configuredKey = configuredKey;
  }

  public boolean isValid(String providedKey) {
    if (configuredKey.isBlank() || providedKey == null || providedKey.isBlank()) return false;
    return MessageDigest.isEqual(configuredKey.getBytes(StandardCharsets.UTF_8),
        providedKey.getBytes(StandardCharsets.UTF_8));
  }
}