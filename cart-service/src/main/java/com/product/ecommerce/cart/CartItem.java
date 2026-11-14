package com.product.ecommerce.cart;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "product_id"}))
public class CartItem {
  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cart_id", nullable = false)
  private ShoppingCart cart;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected CartItem() {}

  CartItem(ShoppingCart cart, UUID productId, int quantity) {
    this.id = UUID.randomUUID();
    this.cart = cart;
    this.productId = productId;
    this.quantity = quantity;
    this.createdAt = Instant.now();
    this.updatedAt = createdAt;
  }

  void addQuantity(int amount) {
    try {
      quantity = Math.addExact(quantity, amount);
    } catch (ArithmeticException exception) {
      throw new IllegalArgumentException("Cart item quantity exceeds the supported limit", exception);
    }
    updatedAt = Instant.now();
  }

  void setQuantity(int quantity) {
    this.quantity = quantity;
    this.updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public UUID getProductId() { return productId; }
  public int getQuantity() { return quantity; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}