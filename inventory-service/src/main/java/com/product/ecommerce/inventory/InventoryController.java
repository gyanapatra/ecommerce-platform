package com.product.ecommerce.inventory;

import com.product.ecommerce.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
  private final InventoryService inventoryService;

  public InventoryController(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @GetMapping("/{productId}")
  public ApiResponse<InventoryResponse> getStock(@PathVariable UUID productId) {
    return ApiResponse.success("Inventory retrieved", inventoryService.getStock(productId));
  }

  @PostMapping("/{productId}/adjustments")
  public ApiResponse<InventoryResponse> adjustStock(
      @PathVariable UUID productId, @Valid @RequestBody StockAdjustmentRequest request) {
    return ApiResponse.success("Stock adjusted", inventoryService.adjustStock(productId, request));
  }

  @PostMapping("/{productId}/reservations")
  public ResponseEntity<ApiResponse<ReservationResponse>> reserve(
      @PathVariable UUID productId, @Valid @RequestBody ReservationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Stock reserved", inventoryService.reserve(productId, request)));
  }

  @DeleteMapping("/{productId}/reservations/{reservationId}")
  public ApiResponse<InventoryResponse> release(
      @PathVariable UUID productId, @PathVariable UUID reservationId) {
    return ApiResponse.success(
        "Reservation released", inventoryService.release(productId, reservationId));
  }

  @PostMapping("/{productId}/reservations/{reservationId}/commit")
  public ApiResponse<InventoryResponse> commit(
      @PathVariable UUID productId, @PathVariable UUID reservationId) {
    return ApiResponse.success(
        "Reservation committed", inventoryService.commit(productId, reservationId));
  }
}
