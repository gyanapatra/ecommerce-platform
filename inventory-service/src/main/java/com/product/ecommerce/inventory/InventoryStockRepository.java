package com.product.ecommerce.inventory;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryStockRepository extends JpaRepository<InventoryStock, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select stock from InventoryStock stock where stock.productId = :productId")
  Optional<InventoryStock> findByProductIdForUpdate(@Param("productId") UUID productId);
}
