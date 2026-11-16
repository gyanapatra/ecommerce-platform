package com.product.ecommerce.notification.repository;

import com.product.ecommerce.notification.domain.CustomerNotification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<CustomerNotification, UUID> {
  boolean existsByEventId(UUID eventId);

  List<CustomerNotification> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

  Optional<CustomerNotification> findByIdAndUserId(UUID id, UUID userId);
}
