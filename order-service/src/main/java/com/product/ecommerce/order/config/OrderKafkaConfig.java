package com.product.ecommerce.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrderKafkaConfig {
  @Bean
  NewTopic orderEventsTopic() {
    return TopicBuilder.name("ecommerce.order-events.v1").partitions(1).replicas(1).build();
  }

  @Bean
  NewTopic orderEventsDeadLetterTopic() {
    return TopicBuilder.name("ecommerce.order-events.v1.DLT").partitions(1).replicas(1).build();
  }
}
