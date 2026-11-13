package com.product.ecommerce.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {
  Optional<Product> findBySku(String sku);

  List<Product> findAllByActiveTrueOrderByNameAsc();

  List<Product> findAllByActiveTrueAndCategoryIgnoreCaseOrderByNameAsc(String category);
}
