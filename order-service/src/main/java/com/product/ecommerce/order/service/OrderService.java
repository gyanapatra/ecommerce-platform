package com.product.ecommerce.order.service;

import com.product.ecommerce.common.exception.ResourceNotFoundException;
import com.product.ecommerce.order.api.OrderResponse;
import com.product.ecommerce.order.client.CartClient;
import com.product.ecommerce.order.client.CartLineSnapshot;
import com.product.ecommerce.order.client.CartSnapshot;
import com.product.ecommerce.order.client.InventoryClient;
import com.product.ecommerce.order.client.ProductClient;
import com.product.ecommerce.order.client.ProductSnapshot;
import com.product.ecommerce.order.domain.CustomerOrder;
import com.product.ecommerce.order.domain.CustomerOrderItem;
import com.product.ecommerce.order.domain.OrderStatus;
import com.product.ecommerce.order.exception.DownstreamServiceException;
import com.product.ecommerce.order.exception.OrderConflictException;
import com.product.ecommerce.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
  private final OrderRepository orders;
  private final OrderPersistenceService orderPersistence;
  private final CartClient cartClient;
  private final ProductClient productClient;
  private final InventoryClient inventoryClient;

  public OrderService(OrderRepository orders, OrderPersistenceService orderPersistence,
      CartClient cartClient, ProductClient productClient, InventoryClient inventoryClient) {
    this.orders = orders;
    this.orderPersistence = orderPersistence;
    this.cartClient = cartClient;
    this.productClient = productClient;
    this.inventoryClient = inventoryClient;
  }

  public OrderResponse checkout(UUID userId, UUID idempotencyKey, String authorization) {
    CustomerOrder existing = orders.findByUserIdAndIdempotencyKey(userId, idempotencyKey).orElse(null);
    if (existing != null) {
      return resumeOrReturn(existing, authorization);
    }

    CartSnapshot cart = cartClient.getCart(authorization);
    List<CartLineSnapshot> cartItems = cart.items() == null ? List.of() : cart.items();
    if (cartItems.isEmpty()) {
      throw new IllegalArgumentException("Cannot place an order from an empty cart");
    }

    CustomerOrder order = new CustomerOrder(userId, idempotencyKey);
    for (CartLineSnapshot cartItem : cartItems) {
      ProductSnapshot product = productClient.getProduct(cartItem.productId());
      if (!product.active()) {
        throw new OrderConflictException("Product is inactive: " + cartItem.productId());
      }
      if (product.price() == null || product.price().compareTo(BigDecimal.ZERO) <= 0) {
        throw new OrderConflictException("Product has an invalid price: " + cartItem.productId());
      }
      order.addItem(product.id(), product.name(), cartItem.quantity(), product.price());
    }

    try {
      order = orders.saveAndFlush(order);
    } catch (DataIntegrityViolationException exception) {
      CustomerOrder concurrent = orders.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
          .orElseThrow(() -> exception);
      return resumeOrReturn(concurrent, authorization);
    }
    return resumeOrReturn(order, authorization);
  }

  public List<OrderResponse> getOrders(UUID userId) {
    return orders.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(OrderResponse::from).toList();
  }

  public OrderResponse getOrder(UUID userId, UUID orderId) {
    return OrderResponse.from(findOwnedOrder(userId, orderId));
  }

  @Transactional
  public OrderResponse beginPayment(UUID userId, UUID orderId) {
    CustomerOrder order = findLockedOwnedOrder(userId, orderId);
    if (order.getStatus() == OrderStatus.PAYMENT_PENDING || order.getStatus() == OrderStatus.PAID) {
      return OrderResponse.from(order);
    }
    if (order.getStatus() != OrderStatus.PLACED) {
      throw new OrderConflictException("Only a placed order can begin payment");
    }
    order.markPaymentPending();
    return OrderResponse.from(orders.saveAndFlush(order));
  }

  @Transactional
  public OrderResponse confirmPayment(UUID userId, UUID orderId) {
    CustomerOrder order = findLockedOwnedOrder(userId, orderId);
    if (order.getStatus() == OrderStatus.PAID) return OrderResponse.from(order);
    if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
      throw new OrderConflictException("Payment must be pending before an order can be marked paid");
    }
    return OrderResponse.from(orderPersistence.markPaidAndRecord(order));
  }
  public OrderResponse cancel(UUID userId, UUID orderId) {
    CustomerOrder order = findOwnedOrder(userId, orderId);
    if (order.getStatus() == OrderStatus.CANCELLED) {
      return OrderResponse.from(order);
    }
    if (order.getStatus() != OrderStatus.PLACED) {
      throw new OrderConflictException("Only a placed order can be cancelled");
    }

    for (CustomerOrderItem item : order.getItems()) {
      inventoryClient.release(item.getProductId(), item.getReservationId());
    }
    return OrderResponse.from(orderPersistence.markCancelledAndRecord(order));
  }

  private OrderResponse resumeOrReturn(CustomerOrder order, String authorization) {
    if (order.getStatus() == OrderStatus.PLACED || order.getStatus() == OrderStatus.PAYMENT_PENDING
        || order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CANCELLED) {
      return OrderResponse.from(order);
    }
    if (order.getStatus() == OrderStatus.REJECTED) {
      throw new OrderConflictException("This idempotency key belongs to a rejected order");
    }
    if (order.getStatus() == OrderStatus.COMPENSATION_REQUIRED) {
      if (!releaseReservations(order)) {
        throw new DownstreamServiceException("Inventory compensation is still pending; retry this checkout");
      }
      order.markRejected();
      orders.saveAndFlush(order);
      throw new OrderConflictException("Order was rejected because inventory could not be reserved");
    }

    try {
      for (CustomerOrderItem item : order.getItems()) {
        inventoryClient.reserve(item.getProductId(), item.getReservationId(), item.getQuantity());
      }
    } catch (OrderConflictException exception) {
      if (releaseReservations(order)) {
        order.markRejected();
      } else {
        order.markCompensationRequired();
      }
      orders.saveAndFlush(order);
      if (order.getStatus() == OrderStatus.COMPENSATION_REQUIRED) {
        throw new DownstreamServiceException("Order failed and inventory compensation is pending", exception);
      }
      throw exception;
    }

    // Clearing the cart is idempotent. If it fails, this order stays PROCESSING and can be retried
    // with the same idempotency key; Inventory reservations also use stable IDs and are idempotent.
    cartClient.clearCart(authorization);
    return OrderResponse.from(orderPersistence.markPlacedAndRecord(order));
  }

  private boolean releaseReservations(CustomerOrder order) {
    boolean complete = true;
    for (CustomerOrderItem item : order.getItems()) {
      try {
        inventoryClient.release(item.getProductId(), item.getReservationId());
      } catch (DownstreamServiceException exception) {
        complete = false;
      }
    }
    return complete;
  }

  private CustomerOrder findLockedOwnedOrder(UUID userId, UUID orderId) {
    return orders.findLockedByIdAndUserId(orderId, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
  }
  private CustomerOrder findOwnedOrder(UUID userId, UUID orderId) {
    return orders.findByIdAndUserId(orderId, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
  }
}



