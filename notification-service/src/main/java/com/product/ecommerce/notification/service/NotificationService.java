package com.product.ecommerce.notification.service;

import com.product.ecommerce.common.event.OrderEvent;
import com.product.ecommerce.common.exception.ResourceNotFoundException;
import com.product.ecommerce.notification.api.NotificationResponse;
import com.product.ecommerce.notification.domain.CustomerNotification;
import com.product.ecommerce.notification.repository.NotificationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
  private final NotificationRepository notifications;

  public NotificationService(NotificationRepository notifications) {
    this.notifications = notifications;
  }

  @Transactional
  public void handleOrderEvent(OrderEvent event) {
    if (notifications.existsByEventId(event.eventId())) {
      return;
    }
    notifications.saveAndFlush(
        new CustomerNotification(
            event.eventId(),
            event.userId(),
            event.orderId(),
            event.eventType(),
            messageFor(event),
            event.occurredAt()));
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> listForUser(UUID userId) {
    return notifications.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(NotificationResponse::from)
        .toList();
  }

  @Transactional
  public NotificationResponse markRead(UUID userId, UUID notificationId) {
    CustomerNotification notification =
        notifications
            .findByIdAndUserId(notificationId, userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Notification not found: " + notificationId));
    notification.markRead();
    return NotificationResponse.from(notifications.save(notification));
  }

  private String messageFor(OrderEvent event) {
    return switch (event.eventType()) {
      case OrderEvent.ORDER_PLACED -> "Your order " + event.orderId() + " has been placed.";
      case OrderEvent.ORDER_PAID -> "Payment for order " + event.orderId() + " was successful.";
      case OrderEvent.ORDER_CANCELLED -> "Your order " + event.orderId() + " has been cancelled.";
      default -> "There is an update for your order " + event.orderId() + ".";
    };
  }
}
