package com.product.ecommerce.order.repository;

import com.product.ecommerce.order.domain.CustomerOrder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {
  @EntityGraph(attributePaths = "items")
  Optional<CustomerOrder> findByUserIdAndIdempotencyKey(UUID userId, UUID idempotencyKey);

  @EntityGraph(attributePaths = "items")
  List<CustomerOrder> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

  @EntityGraph(attributePaths = "items")
  Optional<CustomerOrder> findByIdAndUserId(UUID id, UUID userId);
}