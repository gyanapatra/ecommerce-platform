package com.product.ecommerce.inventory;

import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
    UUID reservationId,
    UUID productId,
    int quantity,
    ReservationStatus status,
    Instant createdAt,
    Instant updatedAt) {
  static ReservationResponse from(InventoryReservation reservation) {
    return new ReservationResponse(
        reservation.getReservationId(),
        reservation.getProductId(),
        reservation.getQuantity(),
        reservation.getStatus(),
        reservation.getCreatedAt(),
        reservation.getUpdatedAt());
  }
}
