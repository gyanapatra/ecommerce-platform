package com.product.ecommerce.notification.api;

import com.product.ecommerce.notification.domain.CustomerNotification;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id, UUID orderId, String eventType, String message, boolean read, Instant createdAt) {
  public static NotificationResponse from(CustomerNotification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getOrderId(),
        notification.getEventType(),
        notification.getMessage(),
        notification.isRead(),
        notification.getCreatedAt());
  }
}
