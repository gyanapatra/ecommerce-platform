package com.product.ecommerce.order.client;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CartSnapshot(UUID cartId, UUID userId, List<CartLineSnapshot> items, Instant updatedAt) {}