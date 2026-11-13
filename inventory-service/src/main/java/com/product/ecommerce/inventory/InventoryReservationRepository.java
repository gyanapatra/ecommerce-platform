package com.product.ecommerce.inventory;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select reservation from InventoryReservation reservation where reservation.reservationId = :reservationId")
  Optional<InventoryReservation> findByReservationIdForUpdate(
      @Param("reservationId") UUID reservationId);
}
