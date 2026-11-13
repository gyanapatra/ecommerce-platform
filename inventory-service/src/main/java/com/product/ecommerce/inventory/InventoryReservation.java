package com.product.ecommerce.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_reservations")
public class InventoryReservation {
  @Id
  @Column(name = "reservation_id", nullable = false)
  private UUID reservationId;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(nullable = false)
  private int quantity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReservationStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected InventoryReservation() {}

  public InventoryReservation(UUID reservationId, UUID productId, int quantity) {
    this.reservationId = reservationId;
    this.productId = productId;
    this.quantity = quantity;
    this.status = ReservationStatus.ACTIVE;
    this.createdAt = Instant.now();
    this.updatedAt = createdAt;
  }

  public void release() {
    status = ReservationStatus.RELEASED;
    updatedAt = Instant.now();
  }

  public void commit() {
    status = ReservationStatus.COMMITTED;
    updatedAt = Instant.now();
  }

  public UUID getReservationId() {
    return reservationId;
  }

  public UUID getProductId() {
    return productId;
  }

  public int getQuantity() {
    return quantity;
  }

  public ReservationStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
