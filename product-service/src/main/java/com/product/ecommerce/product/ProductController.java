package com.product.ecommerce.product;

import com.product.ecommerce.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
  private final ProductCatalogService catalog;

  public ProductController(ProductCatalogService catalog) {
    this.catalog = catalog;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ProductResponse>> create(
      @Valid @RequestBody ProductRequest request) {
    ProductResponse product = catalog.create(request);
    return ResponseEntity.created(URI.create("/api/v1/products/" + product.id()))
        .body(ApiResponse.success("Product created", product));
  }

  @GetMapping("/{id}")
  public ApiResponse<ProductResponse> get(@PathVariable UUID id) {
    return ApiResponse.success("Product retrieved", catalog.get(id));
  }

  @GetMapping
  public ApiResponse<List<ProductResponse>> list(@RequestParam(required = false) String category) {
    return ApiResponse.success("Products retrieved", catalog.list(category));
  }

  @PutMapping("/{id}")
  public ApiResponse<ProductResponse> update(
      @PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
    return ApiResponse.success("Product updated", catalog.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    catalog.delete(id);
    return ResponseEntity.noContent().build();
  }
}
