package dev.synapse.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String TEST_SECRET = "CHANGE_ME_IN_PRODUCTION_THIS_MUST_BE_AT_LEAST_256_BITS_LONG_FOR_HS256";
    private static final long ACCESS_TOKEN_VALIDITY_MS = 900000; // 15 minutes
    private static final long REFRESH_TOKEN_VALIDITY_MS = 604800000; // 7 days

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, ACCESS_TOKEN_VALIDITY_MS, REFRESH_TOKEN_VALIDITY_MS);
    }

    @Test
    void generateAccessToken_shouldReturnValidToken() {
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String role = "USER";

        String token = jwtService.generateAccessToken(userId, username, role);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        // Verify token can be parsed
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(username, claims.get("username"));
        assertEquals(role, claims.get("role"));
        assertEquals("access", claims.get("type"));
    }

    @Test
    void generateRefreshToken_shouldReturnValidToken() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateRefreshToken(userId);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        // Verify token can be parsed
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("refresh", claims.get("type"));
    }

    @Test
    void generateAccessToken_shouldContainIssuedAtAndExpiration() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(userId, "testuser", "USER");

        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void generateAccessToken_shouldCreateDifferentTokensForDifferentUsers() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        String token1 = jwtService.generateAccessToken(userId1, "user1", "USER");
        String token2 = jwtService.generateAccessToken(userId2, "user2", "ADMIN");

        assertNotEquals(token1, token2);
    }

    @Test
    void generateRefreshToken_shouldCreateDifferentTokensForDifferentUsers() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        String token1 = jwtService.generateRefreshToken(userId1);
        String token2 = jwtService.generateRefreshToken(userId2);

        assertNotEquals(token1, token2);
    }

    @Test
    void generateAccessToken_shouldHandleSpecialCharactersInUsername() {
        UUID userId = UUID.randomUUID();
        String username = "test.user+123@example.com";
        String role = "USER";

        String token = jwtService.generateAccessToken(userId, username, role);

        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertEquals(username, claims.get("username"));
    }
}
