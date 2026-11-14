package com.product.ecommerce.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.product.ecommerce.common.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
  @Mock private CartRepository cartRepository;
  @InjectMocks private CartService cartService;

  @Test
  void returnsAnEmptyCartWithoutPersistingIt() {
    UUID userId = UUID.randomUUID();
    when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.empty());

    CartResponse response = cartService.getCart(userId);

    assertEquals(userId, response.userId());
    assertEquals(0, response.items().size());
  }

  @Test
  void addingSameProductIncrementsItsQuantity() {
    UUID userId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    ShoppingCart cart = new ShoppingCart(UUID.randomUUID(), userId);
    cart.addItem(productId, 2);
    when(cartRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(cart));
    when(cartRepository.save(any(ShoppingCart.class))).thenAnswer(call -> call.getArgument(0));

    CartResponse response = cartService.addItem(userId, new AddCartItemRequest(productId, 3));

    assertEquals(1, response.items().size());
    assertEquals(5, response.items().get(0).quantity());
    verify(cartRepository).createIfAbsent(any(UUID.class), org.mockito.ArgumentMatchers.eq(userId));
  }

  @Test
  void updatingMissingCartItemReturnsNotFound() {
    UUID userId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    when(cartRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(new ShoppingCart(UUID.randomUUID(), userId)));

    assertThrows(ResourceNotFoundException.class,
        () -> cartService.setItemQuantity(userId, productId, new SetCartItemQuantityRequest(2)));
  }

  @Test
  void removeIsSafeWhenCartDoesNotExist() {
    UUID userId = UUID.randomUUID();
    when(cartRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.empty());

    CartResponse response = cartService.removeItem(userId, UUID.randomUUID());

    assertEquals(userId, response.userId());
    assertEquals(0, response.items().size());
  }
}