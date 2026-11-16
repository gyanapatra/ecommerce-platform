package com.product.ecommerce.order.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "idempotency_key", nullable = false)
  private UUID idempotencyKey;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private OrderStatus status;

  @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal totalAmount;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  private List<CustomerOrderItem> items = new ArrayList<>();

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(nullable = false)
  private long version;

  protected CustomerOrder() {}

  public CustomerOrder(UUID userId, UUID idempotencyKey) {
    this.id = UUID.randomUUID();
    this.userId = userId;
    this.idempotencyKey = idempotencyKey;
    this.status = OrderStatus.PROCESSING;
    this.totalAmount = BigDecimal.ZERO.setScale(2);
    this.createdAt = Instant.now();
    this.updatedAt = createdAt;
  }

  public void addItem(UUID productId, String productName, int quantity, BigDecimal unitPrice) {
    CustomerOrderItem item = new CustomerOrderItem(this, productId, productName, quantity, unitPrice);
    items.add(item);
    totalAmount = totalAmount.add(item.getLineTotal());
    touch();
  }

  public void markPlaced() {
    requireStatus(OrderStatus.PROCESSING);
    status = OrderStatus.PLACED;
    touch();
  }

  public void markPaymentPending() {
    if (status == OrderStatus.PAYMENT_PENDING) return;
    requireStatus(OrderStatus.PLACED);
    status = OrderStatus.PAYMENT_PENDING;
    touch();
  }

  public void markPaid() {
    if (status == OrderStatus.PAID) return;
    requireStatus(OrderStatus.PAYMENT_PENDING);
    status = OrderStatus.PAID;
    touch();
  }

  public void markCancelled() {
    requireStatus(OrderStatus.PLACED);
    status = OrderStatus.CANCELLED;
    touch();
  }

  public void markRejected() {
    if (status != OrderStatus.PROCESSING && status != OrderStatus.COMPENSATION_REQUIRED) {
      throw new IllegalStateException("Only a processing order can be rejected");
    }
    status = OrderStatus.REJECTED;
    touch();
  }

  public void markCompensationRequired() {
    if (status != OrderStatus.PROCESSING) {
      throw new IllegalStateException("Only a processing order can require compensation");
    }
    status = OrderStatus.COMPENSATION_REQUIRED;
    touch();
  }

  private void requireStatus(OrderStatus expected) {
    if (status != expected) {
      throw new IllegalStateException("Order must be " + expected + " before this transition");
    }
  }

  private void touch() {
    updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public UUID getIdempotencyKey() { return idempotencyKey; }
  public OrderStatus getStatus() { return status; }
  public BigDecimal getTotalAmount() { return totalAmount; }
  public List<CustomerOrderItem> getItems() { return items; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public long getVersion() { return version; }
}
