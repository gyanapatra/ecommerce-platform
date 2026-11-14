package com.product.ecommerce.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.product.ecommerce.order.domain.OutboxEvent;
import com.product.ecommerce.order.repository.OutboxEventRepository;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.support.SendResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {
  @Mock private OutboxEventRepository outbox;
  @Mock private KafkaTemplate<String, String> kafkaTemplate;
  @InjectMocks private OutboxPublisher publisher;

  @Test
  void marksOutboxEventPublishedOnlyAfterKafkaAcknowledges() {
    UUID orderId = UUID.randomUUID();
    OutboxEvent event = new OutboxEvent(UUID.randomUUID(), orderId, "ORDER_PLACED", "{}");
    when(outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of(event));
    CompletableFuture<SendResult<String, String>> acknowledged = CompletableFuture.completedFuture(null);
    when(kafkaTemplate.send("ecommerce.order-events.v1", orderId.toString(), "{}"))
        .thenReturn(acknowledged);

    publisher.publishPending();

    assertNotNull(event.getPublishedAt());
    assertEquals(0, event.getAttempts());
    verify(outbox).save(event);
  }

  @Test
  void recordsFailureAndLeavesOutboxEventPendingForRetry() {
    UUID orderId = UUID.randomUUID();
    OutboxEvent event = new OutboxEvent(UUID.randomUUID(), orderId, "ORDER_PLACED", "{}");
    when(outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of(event));
    CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
    failed.completeExceptionally(new IllegalStateException("broker unavailable"));
    when(kafkaTemplate.send("ecommerce.order-events.v1", orderId.toString(), "{}"))
        .thenReturn(failed);

    publisher.publishPending();

    assertNull(event.getPublishedAt());
    assertEquals(1, event.getAttempts());
    verify(outbox).save(event);
  }
}
