package com.product.ecommerce.product;

import com.product.ecommerce.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductCatalogService {
  private final ProductRepository products;

  public ProductCatalogService(ProductRepository products) {
    this.products = products;
  }

  public ProductResponse create(ProductRequest request) {
    if (products.findBySku(request.sku()).isPresent()) {
      throw new IllegalArgumentException("A product with this SKU already exists");
    }
    return ProductResponse.from(
        products.save(
            new Product(
                request.sku(),
                request.name(),
                request.category(),
                request.description(),
                request.price())));
  }

  @Transactional(readOnly = true)
  public ProductResponse get(UUID id) {
    return ProductResponse.from(find(id));
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> list(String category) {
    List<Product> result =
        (category == null || category.isBlank())
            ? products.findAllByActiveTrueOrderByNameAsc()
            : products.findAllByActiveTrueAndCategoryIgnoreCaseOrderByNameAsc(category.trim());
    return result.stream().map(ProductResponse::from).toList();
  }

  public ProductResponse update(UUID id, ProductRequest request) {
    Product product = find(id);
    products
        .findBySku(request.sku())
        .filter(existing -> !existing.getId().equals(id))
        .ifPresent(existing -> {
          throw new IllegalArgumentException("A product with this SKU already exists");
        });
    product.update(
        request.name(),
        request.category(),
        request.description(),
        request.price(),
        request.active() == null || request.active());
    try {
      return ProductResponse.from(products.save(product));
    } catch (DataIntegrityViolationException exception) {
      throw new IllegalArgumentException("A product with this SKU already exists", exception);
    }
  }

  public void delete(UUID id) {
    Product product = find(id);
    product.update(
        product.getName(),
        product.getCategory(),
        product.getDescription(),
        product.getPrice(),
        false);
    products.save(product);
  }

  private Product find(UUID id) {
    return products
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
  }
}
