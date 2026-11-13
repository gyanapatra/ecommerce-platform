package com.product.ecommerce.inventory;

public class InsufficientStockException extends InventoryConflictException {
  public InsufficientStockException(String message) {
    super(message);
  }
}
