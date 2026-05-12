package dev.synapse.core.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.synapse.core.BaseIntegrationTest;
import dev.synapse.core.dto.CreateUserRequest;
import dev.synapse.core.dto.LoginRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Security validation tests verifying authentication, authorization,
 * rate limiting, CORS, and security headers.
 */
@AutoConfigureMockMvc
class SecurityValidationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create an ADMIN user for tests that need authentication
        adminToken = createTestUser(
            "security-admin",
            "admin@test.local",
            "TestPass123!",
            "ADMIN"
        );
        // Create a regular USER for authorization tests
        userToken = createTestUser(
            "security-user",
            "user@test.local",
            "TestPass123!",
            "USER"
        );
    }

    // -----------------------------------------------------------------------
    // 1. Unauthenticated access
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Unauthenticated access")
    class UnauthenticatedAccess {

        @Test
        @DisplayName("GET /api/health returns 200 without authentication")
        void healthEndpoint_shouldBePublic() throws Exception {
            mockMvc
                .perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        }

        @Test
        @DisplayName("GET /api/users returns 401 without authentication")
        void usersEndpoint_shouldRequireAuth() throws Exception {
            mockMvc
                .perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
        }
    }

    // -----------------------------------------------------------------------
    // 2. Authentication
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Authentication")
    class Authentication {

        @Test
        @DisplayName("POST /api/auth/login with wrong credentials returns 401")
        void login_withWrongCredentials_shouldReturn401() throws Exception {
            LoginRequest badRequest = new LoginRequest(
                "nonexistent",
                "wrongpassword"
            );

            mockMvc
                .perform(
                    post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/auth/login with valid credentials returns 200")
        void login_withValidCredentials_shouldReturn200() throws Exception {
            LoginRequest validRequest = new LoginRequest(
                "security-admin",
                "TestPass123!"
            );

            mockMvc
                .perform(
                    post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
        }
    }

    // -----------------------------------------------------------------------
    // 3. Authorization
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Authorization")
    class Authorization {

        @Test
        @DisplayName("GET /api/audit/events returns 403 for non-ADMIN users")
        void auditEventsEndpoint_shouldRequireAdminRole() throws Exception {
            mockMvc
                .perform(
                    get("/api/audit/events").header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + userToken
                    )
                )
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/audit/events returns 200 for ADMIN users")
        void auditEventsEndpoint_shouldAllowAdmin() throws Exception {
            mockMvc
                .perform(
                    get("/api/audit/events").header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + adminToken
                    )
                )
                .andExpect(status().isOk());
        }
    }

    // -----------------------------------------------------------------------
    // 4. Rate limiting
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Rate limiting")
    class RateLimiting {

        @Test
        @DisplayName("Rate limiting returns 429 after too many requests")
        void excessiveRequests_shouldReturn429() throws Exception {
            // The default rate limit is 60 requests/minute.
            // Send 65 rapid requests to trigger rate limiting.
            int limit = 65;
            int lastStatus = 200;

            for (int i = 0; i < limit; i++) {
                MvcResult result = mockMvc
                    .perform(get("/api/health"))
                    .andReturn();
                lastStatus = result.getResponse().getStatus();
                if (lastStatus == 429) {
                    break;
                }
            }

            assertEquals(
                429,
                lastStatus,
                "Expected 429 after exceeding rate limit"
            );
        }

        @Test
        @DisplayName("Rate limiting returns Retry-After header on 429")
        void rateLimitResponse_shouldIncludeRetryAfterHeader()
            throws Exception {
            int limit = 65;
            MvcResult result = null;

            for (int i = 0; i < limit; i++) {
                result = mockMvc.perform(get("/api/health")).andReturn();
                if (result.getResponse().getStatus() == 429) {
                    break;
                }
            }

            assertNotNull(result);
            assertEquals(429, result.getResponse().getStatus());
            assertNotNull(result.getResponse().getHeader("Retry-After"));
        }
    }

    // -----------------------------------------------------------------------
    // 5. CORS headers
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("CORS headers")
    class CorsHeaders {

        @Test
        @DisplayName("CORS headers are present on OPTIONS preflight response")
        void preflightResponse_shouldIncludeCorsHeaders() throws Exception {
            mockMvc
                .perform(
                    options("/api/health")
                        .header("Origin", "https://example.com")
                        .header("Access-Control-Request-Method", "GET")
                )
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"))
                .andExpect(header().exists("Access-Control-Max-Age"));
        }

        @Test
        @DisplayName("CORS headers are present on GET response")
        void getResponse_shouldIncludeCorsHeaders() throws Exception {
            mockMvc
                .perform(
                    get("/api/health").header("Origin", "https://example.com")
                )
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
        }
    }

    // -----------------------------------------------------------------------
    // 6. Security headers
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Security headers")
    class SecurityHeaders {

        @Test
        @DisplayName("X-Content-Type-Options header is present")
        void response_shouldIncludeXContentTypeOptions() throws Exception {
            mockMvc
                .perform(get("/api/health"))
                .andExpect(
                    header().string("X-Content-Type-Options", "nosniff")
                );
        }

        @Test
        @DisplayName("Strict-Transport-Security header is present")
        void response_shouldIncludeStrictTransportSecurity() throws Exception {
            mockMvc
                .perform(get("/api/health"))
                .andExpect(header().exists("Strict-Transport-Security"));
        }

        @Test
        @DisplayName("X-Frame-Options header is present")
        void response_shouldIncludeXFrameOptions() throws Exception {
            mockMvc
                .perform(get("/api/health"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
        }

        @Test
        @DisplayName("Referrer-Policy header is present")
        void response_shouldIncludeReferrerPolicy() throws Exception {
            mockMvc
                .perform(get("/api/health"))
                .andExpect(header().exists("Referrer-Policy"));
        }

        @Test
        @DisplayName("Permissions-Policy header is present")
        void response_shouldIncludePermissionsPolicy() throws Exception {
            mockMvc
                .perform(get("/api/health"))
                .andExpect(header().exists("Permissions-Policy"));
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Creates a test user via the API and returns a valid JWT access token.
     */
    private String createTestUser(
        String username,
        String email,
        String password,
        String role
    ) throws Exception {
        // Create the user
        CreateUserRequest createRequest = new CreateUserRequest(
            username,
            email,
            password,
            role,
            Map.of()
        );

        mockMvc
            .perform(
                post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest))
            )
            .andDo(result -> {
                // If user already exists, that's fine — we'll log in instead
                if (result.getResponse().getStatus() == 201) {
                    // User created, now log in
                }
            });

        // Log in to get a token
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult loginResult = mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest))
            )
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        // Parse the access token from the response
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(
            responseBody,
            Map.class
        );
        return (String) responseMap.get("accessToken");
    }
}
