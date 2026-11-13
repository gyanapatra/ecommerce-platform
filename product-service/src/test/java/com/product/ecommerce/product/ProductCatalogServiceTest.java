package com.product.ecommerce.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductCatalogServiceTest {
  @Mock private ProductRepository products;
  @InjectMocks private ProductCatalogService catalog;

  @Test
  void createsProductWithActiveStatus() {
    ProductRequest request = request("SKU-1");
    when(products.findBySku("SKU-1")).thenReturn(Optional.empty());
    when(products.save(org.mockito.ArgumentMatchers.any(Product.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ProductResponse result = catalog.create(request);

    assertEquals("SKU-1", result.sku());
    assertEquals(true, result.active());
    verify(products).save(org.mockito.ArgumentMatchers.any(Product.class));
  }

  @Test
  void rejectsDuplicateSku() {
    when(products.findBySku("SKU-1")).thenReturn(Optional.of(new Product(
        "SKU-1", "Existing", "Books", null, BigDecimal.TEN)));

    assertThrows(IllegalArgumentException.class, () -> catalog.create(request("SKU-1")));
  }

  private ProductRequest request(String sku) {
    return new ProductRequest(sku, "Item", "Books", null, BigDecimal.TEN, null);
  }
}
