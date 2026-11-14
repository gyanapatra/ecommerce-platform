package com.product.ecommerce.notification.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.product.ecommerce.common.event.OrderEvent;
import com.product.ecommerce.notification.domain.CustomerNotification;
import com.product.ecommerce.notification.repository.NotificationRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
  @Mock private NotificationRepository notifications;
  @InjectMocks private NotificationService service;

  @Test
  void storesAnOrderNotificationOnlyOnceForRepeatedEvents() {
    UUID eventId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    OrderEvent event = new OrderEvent(eventId, OrderEvent.ORDER_PLACED, orderId, userId,
        "PLACED", new BigDecimal("19.99"), List.of(), Instant.now());
    when(notifications.existsByEventId(eventId)).thenReturn(false, true);

    service.handleOrderEvent(event);
    service.handleOrderEvent(event);

    verify(notifications, times(1))
        .saveAndFlush(org.mockito.ArgumentMatchers.any(CustomerNotification.class));
  }
}
