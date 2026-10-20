package com.product.ecommerce.common.response;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(Instant timestamp, int status, String error, String message, String path,
		String correlationId, Map<String, String> validationErrors) {
}
