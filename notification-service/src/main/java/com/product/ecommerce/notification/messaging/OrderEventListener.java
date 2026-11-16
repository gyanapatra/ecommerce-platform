package com.product.ecommerce.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.product.ecommerce.common.event.OrderEvent;
import com.product.ecommerce.notification.service.NotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    prefix = "app.kafka",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class OrderEventListener {
  private final ObjectMapper objectMapper;
  private final NotificationService notifications;

  public OrderEventListener(ObjectMapper objectMapper, NotificationService notifications) {
    this.objectMapper = objectMapper;
    this.notifications = notifications;
  }

  @KafkaListener(topics = "ecommerce.order-events.v1", groupId = "notification-service")
  public void consume(String payload) throws Exception {
    OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
    notifications.handleOrderEvent(event);
  }
}
