package com.product.ecommerce.order.client;

import com.product.ecommerce.common.exception.ResourceNotFoundException;
import com.product.ecommerce.order.exception.DownstreamServiceException;
import java.util.UUID;
import org.springframework.core.env.Environment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ProductClient {
  private final RestClient client;

  public ProductClient(RestClient.Builder builder, Environment environment) {
    client = builder.baseUrl(environment.getRequiredProperty("services.product.base-url")).build();
  }

  public ProductSnapshot getProduct(UUID productId) {
    try {
      ApiEnvelope<ProductSnapshot> response = client.get().uri("/api/v1/products/{id}", productId)
          .retrieve().body(new ParameterizedTypeReference<>() {});
      if (response == null || !response.success() || response.data() == null) {
        throw new DownstreamServiceException("Product Service returned an invalid response");
      }
      return response.data();
    } catch (HttpClientErrorException.NotFound exception) {
      throw new ResourceNotFoundException("Product not found: " + productId);
    } catch (RestClientException exception) {
      throw new DownstreamServiceException("Product Service is unavailable", exception);
    }
  }
}