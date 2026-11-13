package com.product.ecommerce.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_stock")
public class InventoryStock {
  @Id
  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "on_hand", nullable = false)
  private int onHand;

  @Column(nullable = false)
  private int reserved;

  @Version
  @Column(nullable = false)
  private long version;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected InventoryStock() {}

  public InventoryStock(UUID productId) {
    this.productId = productId;
    this.onHand = 0;
    this.reserved = 0;
    this.updatedAt = Instant.now();
  }

  public int available() {
    return onHand - reserved;
  }

  public void adjust(int quantityDelta) {
    if (quantityDelta == 0) {
      throw new IllegalArgumentException("Quantity delta must not be zero");
    }
    long updatedQuantity = (long) onHand + quantityDelta;
    if (updatedQuantity < reserved) {
      throw new InventoryConflictException(
          "Adjustment cannot reduce stock below reserved quantity");
    }
    if (updatedQuantity > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("On-hand quantity exceeds the supported limit");
    }
    onHand = (int) updatedQuantity;
    updatedAt = Instant.now();
  }

  public void reserve(int quantity) {
    if (quantity > available()) {
      throw new InsufficientStockException("Insufficient available stock");
    }
    reserved += quantity;
    updatedAt = Instant.now();
  }

  public void release(int quantity) {
    if (quantity > reserved) {
      throw new InventoryConflictException("Reserved quantity is inconsistent");
    }
    reserved -= quantity;
    updatedAt = Instant.now();
  }

  public void commit(int quantity) {
    if (quantity > reserved || quantity > onHand) {
      throw new InventoryConflictException("Reserved quantity is inconsistent");
    }
    reserved -= quantity;
    onHand -= quantity;
    updatedAt = Instant.now();
  }

  public UUID getProductId() {
    return productId;
  }

  public int getOnHand() {
    return onHand;
  }

  public int getReserved() {
    return reserved;
  }

  public int getAvailable() {
    return available();
  }

  public long getVersion() {
    return version;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
