package com.product.ecommerce.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(
      ServerHttpSecurity http, ReactiveAuthenticationManager authenticationManager) {

    AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(authenticationManager);

    jwtFilter.setServerAuthenticationConverter(bearerTokenConverter());

    jwtFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

    jwtFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**"));

    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .authorizeExchange(exchange ->
            exchange
                .pathMatchers(
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/logout",
                    "/actuator/health")
                .permitAll()
                .anyExchange()
                .authenticated())
        .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .exceptionHandling(exception ->
            exception.authenticationEntryPoint((exchange, ex) -> {
              exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
              return exchange.getResponse().setComplete();
            }))
        .build();
  }

  private ServerAuthenticationConverter bearerTokenConverter() {

    return exchange -> {
      String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

      if (header == null || !header.startsWith("Bearer ")) {
        return Mono.empty();
      }

      String token = header.substring(7).trim();

      if (token.isEmpty()) {
        return Mono.empty();
      }

      return Mono.just(
          new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
              token, token));
    };
  }
}
