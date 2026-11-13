package com.product.ecommerce.inventory;

import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
    UUID productId, int onHand, int reserved, int available, Instant updatedAt) {
  static InventoryResponse from(InventoryStock stock) {
    return new InventoryResponse(
        stock.getProductId(),
        stock.getOnHand(),
        stock.getReserved(),
        stock.getAvailable(),
        stock.getUpdatedAt());
  }
}
