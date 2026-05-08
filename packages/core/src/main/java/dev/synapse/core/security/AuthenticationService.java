package dev.synapse.core.security;

import dev.synapse.core.domain.User;
import dev.synapse.core.logging.LogCategory;
import dev.synapse.core.logging.LogLevel;
import dev.synapse.core.logging.SystemLogService;
import dev.synapse.core.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final JwtService jwtService;
    private final SystemLogService logService;

    public AuthenticationService(
        UserRepository userRepository,
        PasswordHashingService passwordHashingService,
        JwtService jwtService,
        SystemLogService logService
    ) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.jwtService = jwtService;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordHashingService.verify(password, user.getPasswordHash())) {
            logService.log(
                LogLevel.WARN,
                LogCategory.AUTH,
                Map.of("component", "AuthenticationService", "username", username),
                "LOGIN_FAILED",
                Map.of("reason", "invalid_password"),
                null,
                null
            );
            throw new BadCredentialsException("Invalid username or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        logService.log(
            LogLevel.INFO,
            LogCategory.AUTH,
            Map.of("component", "AuthenticationService", "userId", user.getId().toString()),
            "LOGIN_SUCCESS",
            Map.of("username", username),
            null,
            null
        );

        return new AuthenticationResponse(accessToken, refreshToken, user.getId(), user.getUsername(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String tokenType = jwtService.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BadCredentialsException("Invalid token type");
        }

        UUID userId = jwtService.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        logService.log(
            LogLevel.INFO,
            LogCategory.AUTH,
            Map.of("component", "AuthenticationService", "userId", userId.toString()),
            "TOKEN_REFRESHED",
            Map.of("username", user.getUsername()),
            null,
            null
        );

        return new AuthenticationResponse(newAccessToken, newRefreshToken, user.getId(), user.getUsername(), user.getRole().name());
    }

    public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String username,
        String role
    ) {}
}
