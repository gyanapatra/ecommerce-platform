package com.product.ecommerce.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.product.ecommerce.order.api.OrderResponse;
import com.product.ecommerce.order.client.CartClient;
import com.product.ecommerce.order.client.CartLineSnapshot;
import com.product.ecommerce.order.client.CartSnapshot;
import com.product.ecommerce.order.client.InventoryClient;
import com.product.ecommerce.order.client.ProductClient;
import com.product.ecommerce.order.client.ProductSnapshot;
import com.product.ecommerce.order.domain.CustomerOrder;
import com.product.ecommerce.order.domain.OrderStatus;
import com.product.ecommerce.order.exception.OrderConflictException;
import com.product.ecommerce.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
  private static final String AUTHORIZATION = "Bearer test-token";

  @Mock private OrderRepository orders;
  @Mock private CartClient cartClient;
  @Mock private ProductClient productClient;
  @Mock private InventoryClient inventoryClient;
  @InjectMocks private OrderService orderService;

  @Test
  void checksOutCartSnapshotsProductPricesReservesStockAndClearsCart() {
    UUID userId = UUID.randomUUID();
    UUID idempotencyKey = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    when(orders.findByUserIdAndIdempotencyKey(userId, idempotencyKey)).thenReturn(Optional.empty());
    when(cartClient.getCart(AUTHORIZATION)).thenReturn(cart(userId, new CartLineSnapshot(productId, 2)));
    when(productClient.getProduct(productId)).thenReturn(product(productId, "Headphones", "19.99"));
    when(orders.saveAndFlush(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

    OrderResponse response = orderService.checkout(userId, idempotencyKey, AUTHORIZATION);

    assertEquals(OrderStatus.PLACED, response.status());
    assertEquals(new BigDecimal("39.98"), response.totalAmount());
    assertEquals(1, response.items().size());
    assertEquals(new BigDecimal("19.99"), response.items().get(0).unitPrice());
    verify(inventoryClient).reserve(eq(productId), any(UUID.class), eq(2));
    verify(cartClient).clearCart(AUTHORIZATION);
  }

  @Test
  void returnsExistingOrderForRepeatedIdempotencyKey() {
    UUID userId = UUID.randomUUID();
    UUID key = UUID.randomUUID();
    CustomerOrder existing = new CustomerOrder(userId, key);
    existing.addItem(UUID.randomUUID(), "Keyboard", 1, new BigDecimal("35.00"));
    existing.markPlaced();
    when(orders.findByUserIdAndIdempotencyKey(userId, key)).thenReturn(Optional.of(existing));

    OrderResponse response = orderService.checkout(userId, key, AUTHORIZATION);

    assertEquals(existing.getId(), response.id());
    assertEquals(OrderStatus.PLACED, response.status());
    verify(cartClient, never()).getCart(any());
    verify(inventoryClient, never()).reserve(any(), any(), anyInt());
  }

  @Test
  void compensatesEarlierReservationsWhenAProductCannotBeReserved() {
    UUID userId = UUID.randomUUID();
    UUID key = UUID.randomUUID();
    UUID firstProductId = UUID.randomUUID();
    UUID secondProductId = UUID.randomUUID();
    when(orders.findByUserIdAndIdempotencyKey(userId, key)).thenReturn(Optional.empty());
    when(cartClient.getCart(AUTHORIZATION)).thenReturn(
        cart(userId, new CartLineSnapshot(firstProductId, 1), new CartLineSnapshot(secondProductId, 1)));
    when(productClient.getProduct(firstProductId)).thenReturn(product(firstProductId, "Mouse", "10.00"));
    when(productClient.getProduct(secondProductId)).thenReturn(product(secondProductId, "Monitor", "100.00"));
    when(orders.saveAndFlush(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
    org.mockito.Mockito.doAnswer(invocation -> {
      UUID requestedProductId = invocation.getArgument(0);
      if (requestedProductId.equals(secondProductId)) {
        throw new OrderConflictException("Insufficient stock");
      }
      return null;
    }).when(inventoryClient).reserve(any(UUID.class), any(UUID.class), anyInt());

    assertThrows(OrderConflictException.class, () -> orderService.checkout(userId, key, AUTHORIZATION));

    ArgumentCaptor<CustomerOrder> saved = ArgumentCaptor.forClass(CustomerOrder.class);
    verify(orders, times(2)).saveAndFlush(saved.capture());
    assertEquals(OrderStatus.REJECTED, saved.getAllValues().get(1).getStatus());
    verify(inventoryClient, times(2)).release(any(UUID.class), any(UUID.class));
    verify(cartClient, never()).clearCart(any());
  }

  @Test
  void cancellationReleasesReservationsAndUpdatesStatus() {
    UUID userId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    CustomerOrder order = new CustomerOrder(userId, UUID.randomUUID());
    order.addItem(productId, "Keyboard", 1, new BigDecimal("35.00"));
    order.markPlaced();
    when(orders.findByIdAndUserId(order.getId(), userId)).thenReturn(Optional.of(order));
    when(orders.saveAndFlush(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

    OrderResponse response = orderService.cancel(userId, order.getId());

    assertEquals(OrderStatus.CANCELLED, response.status());
    verify(inventoryClient).release(eq(productId), eq(order.getItems().get(0).getReservationId()));
  }

  private CartSnapshot cart(UUID userId, CartLineSnapshot... items) {
    return new CartSnapshot(UUID.randomUUID(), userId, List.of(items), null);
  }

  private ProductSnapshot product(UUID id, String name, String price) {
    return new ProductSnapshot(id, "SKU-" + id, name, "Tests", "", new BigDecimal(price), true, null, null);
  }
}