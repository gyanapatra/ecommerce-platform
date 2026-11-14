package com.product.ecommerce.cart;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts")
public class ShoppingCart {
  @Id private UUID id;

  @Column(name = "user_id", nullable = false, unique = true)
  private UUID userId;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  private List<CartItem> items = new ArrayList<>();

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(nullable = false)
  private long version;

  protected ShoppingCart() {}

  public ShoppingCart(UUID id, UUID userId) {
    this.id = id;
    this.userId = userId;
    this.createdAt = Instant.now();
    this.updatedAt = createdAt;
  }

  public void addItem(UUID productId, int quantity) {
    items.stream().filter(item -> item.getProductId().equals(productId)).findFirst()
        .ifPresentOrElse(item -> item.addQuantity(quantity),
            () -> items.add(new CartItem(this, productId, quantity)));
    touch();
  }

  public void setItemQuantity(UUID productId, int quantity) {
    findItem(productId).setQuantity(quantity);
    touch();
  }

  public void removeItem(UUID productId) {
    items.removeIf(item -> item.getProductId().equals(productId));
    touch();
  }

  public void clear() {
    items.clear();
    touch();
  }

  private CartItem findItem(UUID productId) {
    return items.stream().filter(item -> item.getProductId().equals(productId)).findFirst()
        .orElseThrow(() -> new com.product.ecommerce.common.exception.ResourceNotFoundException(
            "Product is not present in the cart: " + productId));
  }

  private void touch() { updatedAt = Instant.now(); }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public List<CartItem> getItems() { return items; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public long getVersion() { return version; }
}