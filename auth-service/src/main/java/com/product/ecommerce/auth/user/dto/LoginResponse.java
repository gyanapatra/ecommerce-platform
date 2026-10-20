package com.product.ecommerce.auth.user.dto;

import java.util.UUID;

public record LoginResponse(String accessToken, 
		String refreshToken, 
		String tokenType, 
		long expiresIn, 
		UUID userId,
		String email) {
}
