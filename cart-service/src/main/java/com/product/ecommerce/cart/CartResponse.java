package com.product.ecommerce.cart;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CartResponse(UUID cartId, UUID userId, List<CartItemResponse> items, Instant updatedAt) {
  static CartResponse from(ShoppingCart cart) {
    return new CartResponse(cart.getId(), cart.getUserId(),
        cart.getItems().stream().map(CartItemResponse::from).toList(), cart.getUpdatedAt());
  }

  static CartResponse empty(UUID userId) {
    return new CartResponse(null, userId, List.of(), null);
  }
}