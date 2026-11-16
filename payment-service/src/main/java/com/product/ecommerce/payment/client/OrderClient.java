package com.product.ecommerce.payment.client;

import com.product.ecommerce.common.exception.BusinessException;
import com.product.ecommerce.common.exception.ResourceNotFoundException;
import com.product.ecommerce.common.response.ApiResponse;
import com.product.ecommerce.payment.exception.DownstreamServiceException;
import com.product.ecommerce.payment.exception.PaymentConflictException;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OrderClient {
  private final RestClient client;
  private final String paymentServiceKey;

  public OrderClient(RestClient.Builder builder, Environment environment) {
    client = builder.baseUrl(environment.getRequiredProperty("services.order.base-url")).build();
    paymentServiceKey = environment.getProperty("services.payment-service-key", "");
  }

  public OrderSnapshot getOrder(String authorization, UUID orderId) {
    try {
      ApiResponse<OrderSnapshot> response = client.get().uri("/api/v1/orders/{id}", orderId)
          .header(HttpHeaders.AUTHORIZATION, authorization)
          .retrieve().body(new ParameterizedTypeReference<>() {});
      if (response == null || !response.success() || response.data() == null) {
        throw new DownstreamServiceException("Order Service returned an invalid response");
      }
      return response.data();
    } catch (HttpClientErrorException.NotFound exception) {
      throw new ResourceNotFoundException("Order not found");
    } catch (HttpClientErrorException.Conflict exception) {
      throw new PaymentConflictException("Order Service rejected this payment transition");
    } catch (HttpClientErrorException.Unauthorized exception) {
      throw new BusinessException("Order Service rejected the current authorization");
    } catch (RestClientException exception) {
      throw new DownstreamServiceException("Order Service is unavailable", exception);
    }
  }

  public void confirmPayment(String authorization, UUID orderId) {
    try {
      client.post().uri("/api/v1/orders/{id}/payment-confirmation", orderId)
          .header(HttpHeaders.AUTHORIZATION, authorization)
          .header("X-Payment-Service-Key", paymentServiceKey)
          .retrieve().toBodilessEntity();
    } catch (HttpClientErrorException.Conflict exception) {
      throw new PaymentConflictException("Order Service rejected this payment transition");
    } catch (HttpClientErrorException exception) {
      throw new BusinessException("Order Service could not confirm payment", exception);
    } catch (RestClientException exception) {
      throw new DownstreamServiceException("Order Service is unavailable", exception);
    }
  }
}
