package com.product.ecommerce.cart;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<ShoppingCart, UUID> {
  @EntityGraph(attributePaths = "items")
  Optional<ShoppingCart> findWithItemsByUserId(UUID userId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select cart from ShoppingCart cart where cart.userId = :userId")
  Optional<ShoppingCart> findByUserIdForUpdate(@Param("userId") UUID userId);

  @Modifying
  @Query(value = "INSERT INTO shopping_carts (cart_id, user_id, created_at, updated_at, version) "
      + "VALUES (:cartId, :userId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0) "
      + "ON CONFLICT (user_id) DO NOTHING", nativeQuery = true)
  int createIfAbsent(@Param("cartId") UUID cartId, @Param("userId") UUID userId);
}