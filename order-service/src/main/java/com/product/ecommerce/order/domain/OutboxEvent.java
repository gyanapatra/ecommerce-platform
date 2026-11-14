package com.product.ecommerce.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
  @Id
  @Column(name = "event_id", nullable = false)
  private UUID eventId;

  @Column(name = "aggregate_type", nullable = false, length = 64)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(name = "event_type", nullable = false, length = 64)
  private String eventType;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(nullable = false)
  private int attempts;

  @Column(name = "last_error", length = 2000)
  private String lastError;

  protected OutboxEvent() {}

  public OutboxEvent(UUID eventId, UUID orderId, String eventType, String payload) {
    this.eventId = eventId;
    this.aggregateType = "ORDER";
    this.aggregateId = orderId;
    this.eventType = eventType;
    this.payload = payload;
    this.createdAt = Instant.now();
    this.attempts = 0;
  }

  public void markPublished() {
    publishedAt = Instant.now();
    lastError = null;
  }

  public void markAttemptFailed(String error) {
    attempts++;
    lastError = error == null ? "Unknown publishing error" : error.substring(0, Math.min(error.length(), 2000));
  }

  public UUID getEventId() { return eventId; }
  public UUID getAggregateId() { return aggregateId; }
  public String getEventType() { return eventType; }
  public String getPayload() { return payload; }
  public Instant getPublishedAt() { return publishedAt; }
  public int getAttempts() { return attempts; }
  public String getLastError() { return lastError; }
}
