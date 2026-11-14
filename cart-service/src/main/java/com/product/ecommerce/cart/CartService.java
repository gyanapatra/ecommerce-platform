package com.product.ecommerce.cart;

import com.product.ecommerce.common.exception.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {
  private final CartRepository cartRepository;

  public CartService(CartRepository cartRepository) { this.cartRepository = cartRepository; }

  @Transactional(readOnly = true)
  public CartResponse getCart(UUID userId) {
    return cartRepository.findWithItemsByUserId(userId).map(CartResponse::from)
        .orElseGet(() -> CartResponse.empty(userId));
  }

  public CartResponse addItem(UUID userId, AddCartItemRequest request) {
    ShoppingCart cart = lockOrCreate(userId);
    cart.addItem(request.productId(), request.quantity());
    return CartResponse.from(cartRepository.save(cart));
  }

  public CartResponse setItemQuantity(UUID userId, UUID productId, SetCartItemQuantityRequest request) {
    ShoppingCart cart = lockExisting(userId);
    cart.setItemQuantity(productId, request.quantity());
    return CartResponse.from(cartRepository.save(cart));
  }

  public CartResponse removeItem(UUID userId, UUID productId) {
    ShoppingCart cart = cartRepository.findByUserIdForUpdate(userId).orElse(null);
    if (cart == null) return CartResponse.empty(userId);
    cart.removeItem(productId);
    return CartResponse.from(cartRepository.save(cart));
  }

  public CartResponse clear(UUID userId) {
    ShoppingCart cart = cartRepository.findByUserIdForUpdate(userId).orElse(null);
    if (cart == null) return CartResponse.empty(userId);
    cart.clear();
    return CartResponse.from(cartRepository.save(cart));
  }

  private ShoppingCart lockOrCreate(UUID userId) {
    cartRepository.createIfAbsent(UUID.randomUUID(), userId);
    return cartRepository.findByUserIdForUpdate(userId)
        .orElseThrow(() -> new IllegalStateException("Cart could not be created"));
  }

  private ShoppingCart lockExisting(UUID userId) {
    return cartRepository.findByUserIdForUpdate(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
  }
}