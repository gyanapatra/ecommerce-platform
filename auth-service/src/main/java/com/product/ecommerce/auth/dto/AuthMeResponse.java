package com.product.ecommerce.auth.dto;

public record AuthMeResponse(
        String userId,
        String role) {
}
