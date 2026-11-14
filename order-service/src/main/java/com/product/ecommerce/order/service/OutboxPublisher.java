package com.product.ecommerce.order.service;

import com.product.ecommerce.order.domain.OutboxEvent;
import com.product.ecommerce.order.repository.OutboxEventRepository;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {
  private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
  private final OutboxEventRepository outbox;
  private final KafkaTemplate<String, String> kafkaTemplate;

  public OutboxPublisher(OutboxEventRepository outbox, KafkaTemplate<String, String> kafkaTemplate) {
    this.outbox = outbox;
    this.kafkaTemplate = kafkaTemplate;
  }

  @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
  public void publishPending() {
    for (OutboxEvent event : outbox.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
      try {
        kafkaTemplate.send("ecommerce.order-events.v1", event.getAggregateId().toString(),
                event.getPayload())
            .get(10, TimeUnit.SECONDS);
        event.markPublished();
        outbox.save(event);
      } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        recordFailure(event, exception);
        return;
      } catch (Exception exception) {
        recordFailure(event, exception);
      }
    }
  }

  private void recordFailure(OutboxEvent event, Exception exception) {
    event.markAttemptFailed(exception.getMessage());
    outbox.save(event);
    log.warn("Could not publish order event {}; it remains in the outbox for retry",
        event.getEventId(), exception);
  }
}
