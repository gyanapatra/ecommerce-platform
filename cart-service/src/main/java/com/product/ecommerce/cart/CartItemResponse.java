package com.product.ecommerce.cart;

import java.util.UUID;

public record CartItemResponse(UUID productId, int quantity) {
  static CartItemResponse from(CartItem item) {
    return new CartItemResponse(item.getProductId(), item.getQuantity());
  }
}