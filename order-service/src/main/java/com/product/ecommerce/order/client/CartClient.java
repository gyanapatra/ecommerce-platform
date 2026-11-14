package com.product.ecommerce.order.client;

import com.product.ecommerce.order.exception.DownstreamServiceException;
import org.springframework.core.env.Environment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CartClient {
  private final RestClient client;

  public CartClient(RestClient.Builder builder, Environment environment) {
    client = builder.baseUrl(environment.getRequiredProperty("services.cart.base-url")).build();
  }

  public CartSnapshot getCart(String authorization) {
    try {
      ApiEnvelope<CartSnapshot> response = client.get().uri("/api/v1/cart")
          .header(HttpHeaders.AUTHORIZATION, authorization)
          .retrieve().body(new ParameterizedTypeReference<>() {});
      if (response == null || !response.success() || response.data() == null) {
        throw new DownstreamServiceException("Cart Service returned an invalid response");
      }
      return response.data();
    } catch (RestClientException exception) {
      throw new DownstreamServiceException("Cart Service is unavailable", exception);
    }
  }

  public void clearCart(String authorization) {
    try {
      ApiEnvelope<CartSnapshot> response = client.delete().uri("/api/v1/cart/items")
          .header(HttpHeaders.AUTHORIZATION, authorization)
          .retrieve().body(new ParameterizedTypeReference<>() {});
      if (response == null || !response.success()) {
        throw new DownstreamServiceException("Cart Service could not clear the cart");
      }
    } catch (RestClientException exception) {
      throw new DownstreamServiceException("Cart Service could not clear the cart", exception);
    }
  }
}