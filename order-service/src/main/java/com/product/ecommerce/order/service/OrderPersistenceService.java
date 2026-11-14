package com.product.ecommerce.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.product.ecommerce.common.event.OrderEvent;
import com.product.ecommerce.common.event.OrderLineEvent;
import com.product.ecommerce.order.domain.CustomerOrder;
import com.product.ecommerce.order.domain.CustomerOrderItem;
import com.product.ecommerce.order.domain.OutboxEvent;
import com.product.ecommerce.order.repository.OrderRepository;
import com.product.ecommerce.order.repository.OutboxEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderPersistenceService {
  private final OrderRepository orders;
  private final OutboxEventRepository outbox;
  private final ObjectMapper objectMapper;

  public OrderPersistenceService(OrderRepository orders, OutboxEventRepository outbox,
      ObjectMapper objectMapper) {
    this.orders = orders;
    this.outbox = outbox;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public CustomerOrder markPlacedAndRecord(CustomerOrder order) {
    order.markPlaced();
    return saveWithEvent(order, OrderEvent.ORDER_PLACED);
  }

  @Transactional
  public CustomerOrder markCancelledAndRecord(CustomerOrder order) {
    order.markCancelled();
    return saveWithEvent(order, OrderEvent.ORDER_CANCELLED);
  }

  private CustomerOrder saveWithEvent(CustomerOrder order, String eventType) {
    CustomerOrder saved = orders.saveAndFlush(order);
    OrderEvent event = new OrderEvent(
        UUID.randomUUID(),
        eventType,
        saved.getId(),
        saved.getUserId(),
        saved.getStatus().name(),
        saved.getTotalAmount(),
        saved.getItems().stream().map(this::toLineEvent).toList(),
        Instant.now());
    try {
      outbox.save(new OutboxEvent(event.eventId(), saved.getId(), eventType,
          objectMapper.writeValueAsString(event)));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Could not serialize order event", exception);
    }
    return saved;
  }

  private OrderLineEvent toLineEvent(CustomerOrderItem item) {
    return new OrderLineEvent(item.getProductId(), item.getProductName(), item.getQuantity(),
        item.getUnitPrice(), item.getLineTotal());
  }
}
