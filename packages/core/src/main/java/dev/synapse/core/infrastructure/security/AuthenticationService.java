package dev.synapse.core.infrastructure.security;

import dev.synapse.core.common.domain.User;
import dev.synapse.core.common.repository.UserRepository;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final JwtService jwtService;
    private final SystemLogService logService;

    private final int accountLockoutThreshold;
    private final int accountLockoutDurationMinutes;
    private final ConcurrentHashMap<
        String,
        FailedLoginAttempt
    > failedLoginAttempts = new ConcurrentHashMap<>();

    public AuthenticationService(
        UserRepository userRepository,
        PasswordHashingService passwordHashingService,
        JwtService jwtService,
        SystemLogService logService,
        @Value(
            "${rate-limiting.account-lockout-threshold:5}"
        ) int accountLockoutThreshold,
        @Value(
            "${rate-limiting.account-lockout-duration-minutes:15}"
        ) int accountLockoutDurationMinutes
    ) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.jwtService = jwtService;
        this.logService = logService;
        this.accountLockoutThreshold = accountLockoutThreshold;
        this.accountLockoutDurationMinutes = accountLockoutDurationMinutes;
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(String username, String password) {
        // Check if the account is currently locked due to too many failed attempts
        checkAccountLockout(username);

        User user = userRepository
            .findByUsername(username)
            .orElseThrow(() -> {
                recordFailedAttempt(username);
                return new BadCredentialsException(
                    "Invalid username or password"
                );
            });

        if (!passwordHashingService.verify(password, user.getPasswordHash())) {
            recordFailedAttempt(username);

            logService.log(
                LogLevel.WARN,
                LogCategory.AUTH,
                Map.of(
                    "component",
                    "AuthenticationService",
                    "username",
                    username
                ),
                "LOGIN_FAILED",
                Map.of("reason", "invalid_password"),
                null,
                null
            );
            throw new BadCredentialsException("Invalid username or password");
        }

        // Successful login — reset failed attempts for this user
        failedLoginAttempts.remove(username);

        String accessToken = jwtService.generateAccessToken(
            user.getId(),
            user.getUsername(),
            user.getRole().name()
        );
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        logService.log(
            LogLevel.INFO,
            LogCategory.AUTH,
            Map.of(
                "component",
                "AuthenticationService",
                "userId",
                user.getId().toString()
            ),
            "LOGIN_SUCCESS",
            Map.of("username", username),
            null,
            null
        );

        return new AuthenticationResponse(
            accessToken,
            refreshToken,
            user.getId(),
            user.getUsername(),
            user.getRole().name()
        );
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BadCredentialsException(
                "Invalid or expired refresh token"
            );
        }

        String tokenType = jwtService.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BadCredentialsException("Invalid token type");
        }

        UUID userId = jwtService.extractUserId(refreshToken);
        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(
            user.getId(),
            user.getUsername(),
            user.getRole().name()
        );
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        logService.log(
            LogLevel.INFO,
            LogCategory.AUTH,
            Map.of(
                "component",
                "AuthenticationService",
                "userId",
                userId.toString()
            ),
            "TOKEN_REFRESHED",
            Map.of("username", user.getUsername()),
            null,
            null
        );

        return new AuthenticationResponse(
            newAccessToken,
            newRefreshToken,
            user.getId(),
            user.getUsername(),
            user.getRole().name()
        );
    }

    private void checkAccountLockout(String username) {
        FailedLoginAttempt attempt = failedLoginAttempts.get(username);
        if (attempt != null && attempt.isLocked()) {
            long remainingMinutes = Duration.between(
                Instant.now(),
                attempt.lockoutUntil
            ).toMinutes();
            logService.log(
                LogLevel.WARN,
                LogCategory.AUTH,
                Map.of(
                    "component",
                    "AuthenticationService",
                    "username",
                    username
                ),
                "ACCOUNT_LOCKED",
                Map.of(
                    "reason",
                    "too_many_attempts",
                    "remainingMinutes",
                    String.valueOf(remainingMinutes)
                ),
                null,
                null
            );
            throw new BadCredentialsException(
                "Account is temporarily locked due to too many failed attempts. Please try again later."
            );
        }
    }

    private void recordFailedAttempt(String username) {
        failedLoginAttempts.compute(username, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                FailedLoginAttempt newAttempt = new FailedLoginAttempt();
                newAttempt.count.incrementAndGet();
                return newAttempt;
            }
            existing.count.incrementAndGet();
            if (existing.count.get() >= accountLockoutThreshold) {
                existing.lockoutUntil = Instant.now().plus(
                    Duration.ofMinutes(accountLockoutDurationMinutes)
                );
                logService.log(
                    LogLevel.WARN,
                    LogCategory.AUTH,
                    Map.of(
                        "component",
                        "AuthenticationService",
                        "username",
                        username
                    ),
                    "ACCOUNT_LOCKOUT_TRIGGERED",
                    Map.of(
                        "threshold",
                        String.valueOf(accountLockoutThreshold),
                        "durationMinutes",
                        String.valueOf(accountLockoutDurationMinutes)
                    ),
                    null,
                    null
                );
            }
            return existing;
        });
    }

    private static class FailedLoginAttempt {

        final AtomicInteger count = new AtomicInteger(0);
        volatile Instant lockoutUntil = null;
        volatile Instant firstAttempt = Instant.now();

        boolean isExpired() {
            return (
                Duration.between(firstAttempt, Instant.now()).toMinutes() >= 15
            );
        }

        boolean isLocked() {
            return lockoutUntil != null && Instant.now().isBefore(lockoutUntil);
        }
    }

    public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String username,
        String role
    ) {}
}
