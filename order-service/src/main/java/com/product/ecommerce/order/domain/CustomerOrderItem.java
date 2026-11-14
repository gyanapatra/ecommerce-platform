package com.product.ecommerce.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_items", uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "product_id"}))
public class CustomerOrderItem {
  @Id
  @Column(nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private CustomerOrder order;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "product_name", nullable = false, length = 200)
  private String productName;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "line_total", nullable = false, precision = 19, scale = 2)
  private BigDecimal lineTotal;

  @Column(name = "reservation_id", nullable = false, unique = true)
  private UUID reservationId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected CustomerOrderItem() {}

  CustomerOrderItem(CustomerOrder order, UUID productId, String productName, int quantity, BigDecimal unitPrice) {
    this.id = UUID.randomUUID();
    this.order = order;
    this.productId = productId;
    this.productName = productName;
    this.quantity = quantity;
    this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    this.reservationId = UUID.randomUUID();
    this.createdAt = Instant.now();
  }

  public UUID getId() { return id; }
  public UUID getProductId() { return productId; }
  public String getProductName() { return productName; }
  public int getQuantity() { return quantity; }
  public BigDecimal getUnitPrice() { return unitPrice; }
  public BigDecimal getLineTotal() { return lineTotal; }
  public UUID getReservationId() { return reservationId; }
  public Instant getCreatedAt() { return createdAt; }
}