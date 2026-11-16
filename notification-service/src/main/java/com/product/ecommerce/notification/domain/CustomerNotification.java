package com.product.ecommerce.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_notifications")
public class CustomerNotification {
  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(name = "event_id", nullable = false, unique = true)
  private UUID eventId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Column(name = "event_type", nullable = false, length = 64)
  private String eventType;

  @Column(nullable = false, length = 500)
  private String message;

  @Column(name = "is_read", nullable = false)
  private boolean read;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected CustomerNotification() {}

  public CustomerNotification(
      UUID eventId,
      UUID userId,
      UUID orderId,
      String eventType,
      String message,
      Instant createdAt) {
    this.id = UUID.randomUUID();
    this.eventId = eventId;
    this.userId = userId;
    this.orderId = orderId;
    this.eventType = eventType;
    this.message = message;
    this.createdAt = createdAt;
    this.read = false;
  }

  public void markRead() {
    read = true;
  }

  public UUID getId() {
    return id;
  }

  public UUID getEventId() {
    return eventId;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public String getEventType() {
    return eventType;
  }

  public String getMessage() {
    return message;
  }

  public boolean isRead() {
    return read;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
