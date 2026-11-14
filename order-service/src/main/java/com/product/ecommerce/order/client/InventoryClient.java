package com.product.ecommerce.order.client;

import com.product.ecommerce.order.exception.DownstreamServiceException;
import com.product.ecommerce.order.exception.OrderConflictException;
import java.util.UUID;
import org.springframework.core.env.Environment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class InventoryClient {
  private final RestClient client;

  public InventoryClient(RestClient.Builder builder, Environment environment) {
    client = builder.baseUrl(environment.getRequiredProperty("services.inventory.base-url")).build();
  }

  public void reserve(UUID productId, UUID reservationId, int quantity) {
    try {
      ApiEnvelope<Object> response = client.post()
          .uri("/api/v1/inventory/{productId}/reservations", productId)
          .body(new ReservationRequest(reservationId, quantity))
          .retrieve().body(new ParameterizedTypeReference<>() {});
      if (response == null || !response.success()) {
        throw new DownstreamServiceException("Inventory Service returned an invalid reservation response");
      }
    } catch (HttpClientErrorException.Conflict exception) {
      throw new OrderConflictException("Insufficient stock for product: " + productId);
    } catch (HttpClientErrorException.NotFound exception) {
      throw new OrderConflictException("Inventory has not been initialized for product: " + productId);
    } catch (RestClientException exception) {
      throw new DownstreamServiceException("Inventory Service is unavailable", exception);
    }
  }

  public void release(UUID productId, UUID reservationId) {
    try {
      client.delete()
          .uri("/api/v1/inventory/{productId}/reservations/{reservationId}", productId, reservationId)
          .retrieve().toBodilessEntity();
    } catch (HttpClientErrorException.NotFound exception) {
      // A reservation may not have been created if the reserve call failed before reaching Inventory.
    } catch (RestClientException exception) {
      throw new DownstreamServiceException("Inventory Service could not release reservation " + reservationId, exception);
    }
  }

  private record ReservationRequest(UUID reservationId, int quantity) {}
}