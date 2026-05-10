package dev.synapse.core.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashingServiceTest {

    private PasswordHashingService passwordHashingService;

    @BeforeEach
    void setUp() {
        passwordHashingService = new PasswordHashingService();
    }

    @Test
    void hash_shouldReturnNonNullHash() {
        String password = "mySecurePassword123!";
        String hash = passwordHashingService.hash(password);
        
        assertNotNull(hash);
        assertTrue(hash.startsWith("$argon2id$"));
    }

    @Test
    void hash_shouldProduceDifferentHashesForSamePassword() {
        String password = "mySecurePassword123!";
        String hash1 = passwordHashingService.hash(password);
        String hash2 = passwordHashingService.hash(password);
        
        assertNotEquals(hash1, hash2, "Hashes should differ due to random salt");
    }

    @Test
    void verify_shouldReturnTrueForCorrectPassword() {
        String password = "mySecurePassword123!";
        String hash = passwordHashingService.hash(password);
        
        assertTrue(passwordHashingService.verify(password, hash));
    }

    @Test
    void verify_shouldReturnFalseForIncorrectPassword() {
        String password = "mySecurePassword123!";
        String wrongPassword = "wrongPassword";
        String hash = passwordHashingService.hash(password);
        
        assertFalse(passwordHashingService.verify(wrongPassword, hash));
    }

    @Test
    void verify_shouldReturnFalseForMalformedHash() {
        String password = "mySecurePassword123!";
        String malformedHash = "invalid-hash-format";
        
        assertFalse(passwordHashingService.verify(password, malformedHash));
    }

    @Test
    void verify_shouldHandleEmptyPassword() {
        String emptyPassword = "";
        String hash = passwordHashingService.hash(emptyPassword);
        
        assertTrue(passwordHashingService.verify(emptyPassword, hash));
        assertFalse(passwordHashingService.verify("notEmpty", hash));
    }

    @Test
    void hash_shouldHandleSpecialCharacters() {
        String complexPassword = "p@$$w0rd!@#$%^&*()_+-=[]{}|;':\",./<>?";
        String hash = passwordHashingService.hash(complexPassword);
        
        assertNotNull(hash);
        assertTrue(passwordHashingService.verify(complexPassword, hash));
    }

    @Test
    void hash_shouldHandleUnicodeCharacters() {
        String unicodePassword = "пароль密码🔐";
        String hash = passwordHashingService.hash(unicodePassword);
        
        assertNotNull(hash);
        assertTrue(passwordHashingService.verify(unicodePassword, hash));
    }
}
