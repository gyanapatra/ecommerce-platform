package com.product.ecommerce.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.product.ecommerce.order.domain.CustomerOrder;
import com.product.ecommerce.order.domain.OrderStatus;
import com.product.ecommerce.order.repository.OrderRepository;
import com.product.ecommerce.order.repository.OutboxEventRepository;
import com.product.ecommerce.order.service.OrderPersistenceService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class OrderPersistenceIntegrationTest {
  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("ecommerce_order_test")
          .withUsername("test_user")
          .withPassword("test_password");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("app.kafka.enabled", () -> false);
    registry.add("app.jwt.secret",
        () -> "dGVzdC1vbmx5LXNlY3JldC1rZXktZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ1Ng==");
  }

  @Autowired private OrderRepository orders;
  @Autowired private OutboxEventRepository outbox;
  @Autowired private OrderPersistenceService orderPersistence;

  @Test
  void persistsOrderAndOutboxEventInOneTransaction() {
    UUID userId = UUID.randomUUID();
    UUID idempotencyKey = UUID.randomUUID();
    CustomerOrder order = new CustomerOrder(userId, idempotencyKey);
    order.addItem(UUID.randomUUID(), "Postgres Schema Test", 2, new BigDecimal("12.50"));

    CustomerOrder saved = orderPersistence.markPlacedAndRecord(order);
    CustomerOrder loaded = orders.findByUserIdAndIdempotencyKey(userId, idempotencyKey).orElseThrow();

    assertNotNull(saved.getId());
    assertEquals(OrderStatus.PLACED, loaded.getStatus());
    assertEquals(new BigDecimal("25.00"), loaded.getTotalAmount());
    assertEquals(1, loaded.getItems().size());
    assertEquals("Postgres Schema Test", loaded.getItems().get(0).getProductName());
    assertEquals(new BigDecimal("12.50"), loaded.getItems().get(0).getUnitPrice());
    assertEquals(1, outbox.count());
    assertTrue(outbox.findAll().get(0).getPayload().contains("ORDER_PLACED"));
  }
}
