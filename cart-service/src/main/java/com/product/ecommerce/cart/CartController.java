package com.product.ecommerce.cart;

import com.product.ecommerce.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
  private final CartService cartService;

  public CartController(CartService cartService) { this.cartService = cartService; }

  @GetMapping
  public ApiResponse<CartResponse> getCart(Principal principal) {
    return ApiResponse.success("Cart retrieved", cartService.getCart(userId(principal)));
  }

  @PostMapping("/items")
  public ApiResponse<CartResponse> addItem(Principal principal, @Valid @RequestBody AddCartItemRequest request) {
    return ApiResponse.success("Item added to cart", cartService.addItem(userId(principal), request));
  }

  @PutMapping("/items/{productId}")
  public ApiResponse<CartResponse> setQuantity(Principal principal, @PathVariable UUID productId,
      @Valid @RequestBody SetCartItemQuantityRequest request) {
    return ApiResponse.success("Cart item quantity updated", cartService.setItemQuantity(userId(principal), productId, request));
  }

  @DeleteMapping("/items/{productId}")
  public ApiResponse<CartResponse> removeItem(Principal principal, @PathVariable UUID productId) {
    return ApiResponse.success("Item removed from cart", cartService.removeItem(userId(principal), productId));
  }

  @DeleteMapping("/items")
  public ApiResponse<CartResponse> clear(Principal principal) {
    return ApiResponse.success("Cart cleared", cartService.clear(userId(principal)));
  }

  private UUID userId(Principal principal) { return UUID.fromString(principal.getName()); }
}