package com.product.ecommerce.order.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductSnapshot(
    UUID id, String sku, String name, String category, String description,
    BigDecimal price, boolean active, Instant createdAt, Instant updatedAt) {}