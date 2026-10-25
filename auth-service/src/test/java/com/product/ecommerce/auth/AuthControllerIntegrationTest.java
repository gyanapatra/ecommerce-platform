package com.product.ecommerce.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

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

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerUser_shouldReturnCreated() throws Exception {
        String request = """
                {
                    "email": "integration@example.com",
                    "password": "StrongPassword123!",
                    "firstName": "Integration",
                    "lastName": "Tester"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("integration@example.com"));
    }

    @Test
    void registerDuplicateEmail_shouldBeRejected() throws Exception {
        String request = """
                {
                    "email": "duplicate@example.com",
                    "password": "StrongPassword123!",
                    "firstName": "Duplicate",
                    "lastName": "Tester"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void loginWithValidCredentials_shouldReturnTokens() throws Exception {
        String registrationRequest = """
                {
                    "email": "login@example.com",
                    "password": "StrongPassword123!",
                    "firstName": "Login",
                    "lastName": "Tester"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
                {
                    "email": "login@example.com",
                    "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginWithInvalidPassword_shouldBeRejected() throws Exception {
        String registrationRequest = """
                {
                    "email": "invalid-password@example.com",
                    "password": "StrongPassword123!",
                    "firstName": "Invalid",
                    "lastName": "Password"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequest))
                .andExpect(status().isCreated());

        String loginRequest = """
                {
                    "email": "invalid-password@example.com",
                    "password": "WrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }
}
