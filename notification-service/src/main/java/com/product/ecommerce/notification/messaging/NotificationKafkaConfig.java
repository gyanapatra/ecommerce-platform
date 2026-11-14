package com.product.ecommerce.notification.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.ConsumerFactory;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificationKafkaConfig {
  @Bean
  NewTopic orderEventsTopic() {
    return TopicBuilder.name("ecommerce.order-events.v1").partitions(1).replicas(1).build();
  }

  @Bean
  NewTopic orderEventsDeadLetterTopic() {
    return TopicBuilder.name("ecommerce.order-events.v1.DLT").partitions(1).replicas(1).build();
  }

  @Bean
  DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
        kafkaTemplate,
        (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
    return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
  }

  @Bean
  ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
      ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
      ConsumerFactory<String, String> consumerFactory,
      DefaultErrorHandler kafkaErrorHandler) {
    ConcurrentKafkaListenerContainerFactory<String, String> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
    factory.setCommonErrorHandler(kafkaErrorHandler);
    return factory;
  }
}


