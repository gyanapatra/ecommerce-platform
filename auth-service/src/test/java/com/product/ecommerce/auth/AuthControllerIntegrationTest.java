package com.product.ecommerce.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("ecommerce_auth_test")
          .withUsername("test_user")
          .withPassword("test_password");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);

    // Use a dedicated test-only JWT secret.
    registry.add(
        "app.jwt.secret",
        () -> "dGVzdC1vbmx5LXNlY3JldC1rZXktZm9yLWludGVncmF0aW9uLXRlc3RzLTEyMzQ1Ng==");
  }

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void registerUser_shouldReturnCreated() throws Exception {
    String request =
        """
                {
                    "email": "integration@example.com",
                    "password": "StrongPassword123!",
                    "firstName": "Integration",
                    "lastName": "Tester"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("integration@example.com"));
  }

  @Test
  void registerDuplicateEmail_shouldBeRejected() throws Exception {
    String request =
        """
                {
                    "email": "duplicate@example.com",
                    "password": "StrongPassword123!",
                    "firstName": "Duplicate",
                    "lastName": "Tester"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(request))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void loginWithValidCredentials_shouldReturnTokens() throws Exception {
    String registrationRequest =
        """
                {
                    "email": "login@example.com",
                    "password": "StrongPassword123!",
                    "firstName": "Login",
                    "lastName": "Tester"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationRequest))
        .andExpect(status().isCreated());

    String loginRequest =
        """
                {
                    "email": "login@example.com",
                    "password": "StrongPassword123!"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.refreshToken").isNotEmpty())
        .andExpect(jsonPath("$.tokenType").value("Bearer"));
  }

  @Test
  void loginWithInvalidPassword_shouldBeRejected() throws Exception {
    String registrationRequest =
        """
                {
                    "email": "invalid-password@example.com",
                    "password": "StrongPassword123!",
                    "firstName": "Invalid",
                    "lastName": "Password"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationRequest))
        .andExpect(status().isCreated());

    String loginRequest =
        """
                {
                    "email": "invalid-password@example.com",
                    "password": "WrongPassword123!"
                }
                """;

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void refreshTokenRotationRejectsReuseAndRevokesTheTokenFamily() throws Exception {
    String refreshTokenA = registerAndLoginForRefreshFlow();

    MvcResult rotationResult =
        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(tokenRequest(refreshTokenA)))
            .andExpect(status().isOk())
            .andReturn();
    String refreshTokenB =
        objectMapper
            .readTree(rotationResult.getResponse().getContentAsString())
            .path("refreshToken")
            .asText();
    assertNotEquals(refreshTokenA, refreshTokenB);

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenRequest(refreshTokenA)))
        .andExpect(status().isUnauthorized());

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenRequest(refreshTokenB)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void logoutRevokesTheWholeRefreshTokenFamily() throws Exception {
    String refreshTokenA = registerAndLoginForRefreshFlow();
    MvcResult rotationResult =
        mockMvc
            .perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(tokenRequest(refreshTokenA)))
            .andExpect(status().isOk())
            .andReturn();
    String refreshTokenB =
        objectMapper
            .readTree(rotationResult.getResponse().getContentAsString())
            .path("refreshToken")
            .asText();

    mockMvc
        .perform(
            post("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenRequest(refreshTokenB)))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenRequest(refreshTokenA)))
        .andExpect(status().isUnauthorized());

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenRequest(refreshTokenB)))
        .andExpect(status().isUnauthorized());
  }

  private String registerAndLoginForRefreshFlow() throws Exception {
    String email = "refresh-" + UUID.randomUUID() + "@example.com";
    String password = "StrongPassword123!";
    String registrationRequest =
        """
            {
              "email": "%s",
              "password": "%s",
              "firstName": "Refresh",
              "lastName": "Tester"
            }
            """.formatted(email, password);

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationRequest))
        .andExpect(status().isCreated());

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode response = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    return response.path("refreshToken").asText();
  }

  private String tokenRequest(String refreshToken) throws Exception {
    return objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken));
  }
}


