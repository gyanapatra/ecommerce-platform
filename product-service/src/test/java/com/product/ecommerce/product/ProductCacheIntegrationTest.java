package com.product.ecommerce.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class ProductCacheIntegrationTest {
  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("ecommerce_product_test")
          .withUsername("test_user")
          .withPassword("test_password");

  @Container
  static final GenericContainer<?> REDIS =
      new GenericContainer<>(DockerImageName.parse("redis:7.4.12-alpine"))
          .withExposedPorts(6379);

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
  }

  @Autowired private ProductCatalogService catalog;
  @Autowired private ProductRepository products;
  @Autowired private CacheManager cacheManager;

  @Test
  void cachesReadResultsAndEvictsEntriesAfterProductWrites() {
    ProductResponse created = catalog.create(request("SKU-CACHE-1", "Initial name"));
    assertEquals("Initial name", catalog.get(created.id()).name());
    assertNotNull(cacheManager.getCache("product-by-id").get(created.id()));

    Product stored = products.findById(created.id()).orElseThrow();
    stored.update("Database-only name", "Books", null, BigDecimal.TEN, true);
    products.saveAndFlush(stored);
    assertEquals("Initial name", catalog.get(created.id()).name());

    catalog.update(created.id(), request("SKU-CACHE-1", "Updated name"));
    assertEquals("Updated name", catalog.get(created.id()).name());

    catalog.list(null);
    assertNotNull(cacheManager.getCache("product-list").get(""));
    catalog.create(request("SKU-CACHE-2", "Second item"));
    assertEquals(2, catalog.list(null).size());
  }

  private ProductRequest request(String sku, String name) {
    return new ProductRequest(sku, name, "Books", null, BigDecimal.TEN, null);
  }
}
