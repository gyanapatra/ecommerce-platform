package com.product.ecommerce.payment.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
  Optional<Payment> findByUserIdAndIdempotencyKey(UUID userId, UUID idempotencyKey);
  Optional<Payment> findByOrderId(UUID orderId);
  Optional<Payment> findByIdAndUserId(UUID id, UUID userId);
}