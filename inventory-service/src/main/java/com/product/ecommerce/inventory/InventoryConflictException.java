package com.product.ecommerce.inventory;

public class InventoryConflictException extends RuntimeException {
  public InventoryConflictException(String message) {
    super(message);
  }
}
