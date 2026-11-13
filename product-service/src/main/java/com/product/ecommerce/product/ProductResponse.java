package com.product.ecommerce.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String sku,
    String name,
    String category,
    String description,
    BigDecimal price,
    boolean active,
    Instant createdAt,
    Instant updatedAt) {
  static ProductResponse from(Product product) {
    return new ProductResponse(
        product.getId(),
        product.getSku(),
        product.getName(),
        product.getCategory(),
        product.getDescription(),
        product.getPrice(),
        product.isActive(),
        product.getCreatedAt(),
        product.getUpdatedAt());
  }
}
