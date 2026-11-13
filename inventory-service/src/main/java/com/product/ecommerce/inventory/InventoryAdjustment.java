package com.product.ecommerce.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_adjustments")
public class InventoryAdjustment {
  @Id
  @Column(name = "adjustment_id", nullable = false)
  private UUID adjustmentId;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "quantity_delta", nullable = false)
  private int quantityDelta;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected InventoryAdjustment() {}

  public InventoryAdjustment(UUID adjustmentId, UUID productId, int quantityDelta) {
    this.adjustmentId = adjustmentId;
    this.productId = productId;
    this.quantityDelta = quantityDelta;
    this.createdAt = Instant.now();
  }

  public UUID getAdjustmentId() {
    return adjustmentId;
  }

  public UUID getProductId() {
    return productId;
  }

  public int getQuantityDelta() {
    return quantityDelta;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
