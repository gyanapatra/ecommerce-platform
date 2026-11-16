package com.product.ecommerce.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_payment_user_idempotency", columnNames = {"user_id", "idempotency_key"}),
    @UniqueConstraint(name = "uk_payment_order", columnNames = "order_id")
})
public class Payment {
  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @Column(name = "idempotency_key", nullable = false)
  private UUID idempotencyKey;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 24)
  private PaymentStatus status;

  @Column(nullable = false, length = 24)
  private String provider;

  @Column(name = "provider_reference", length = 128)
  private String providerReference;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(nullable = false)
  private long version;

  protected Payment() {}

  public Payment(UUID userId, UUID orderId, UUID idempotencyKey, BigDecimal amount, String currency) {
    this.id = UUID.randomUUID();
    this.userId = userId;
    this.orderId = orderId;
    this.idempotencyKey = idempotencyKey;
    this.amount = amount;
    this.currency = currency;
    this.status = PaymentStatus.PROCESSING;
    this.provider = "MOCK";
    this.createdAt = Instant.now();
    this.updatedAt = createdAt;
  }

  public void markSucceeded(String providerReference) {
    if (status == PaymentStatus.SUCCEEDED) return;
    status = PaymentStatus.SUCCEEDED;
    this.providerReference = providerReference;
    updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public UUID getOrderId() { return orderId; }
  public UUID getIdempotencyKey() { return idempotencyKey; }
  public BigDecimal getAmount() { return amount; }
  public String getCurrency() { return currency; }
  public PaymentStatus getStatus() { return status; }
  public String getProvider() { return provider; }
  public String getProviderReference() { return providerReference; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}