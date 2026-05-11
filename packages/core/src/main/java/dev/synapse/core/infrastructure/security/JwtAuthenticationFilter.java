package dev.synapse.core.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(
        JwtAuthenticationFilter.class
    );

    private final JwtService jwtService;
    private final SecurityAuditService auditService;

    public JwtAuthenticationFilter(
        JwtService jwtService,
        SecurityAuditService auditService
    ) {
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String ipAddress = extractClientIp(request);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (jwtService.isTokenValid(token)) {
                    String tokenType = jwtService.extractTokenType(token);

                    if ("access".equals(tokenType)) {
                        UUID userId = jwtService.extractUserId(token);
                        String username = jwtService.extractUsername(token);
                        String role = jwtService.extractRole(token);

                        List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role)
                        );

                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities
                            );

                        authentication.setDetails(
                            new JwtAuthenticationDetails(userId, username, role)
                        );

                        SecurityContextHolder.getContext().setAuthentication(
                            authentication
                        );
                    }
                } else {
                    // Token is present but invalid or expired
                    logAuthorizationFailure(token, ipAddress);
                }
            } catch (Exception e) {
                // Invalid token, continue without authentication
                logAuthorizationFailure(token, ipAddress);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void logAuthorizationFailure(String token, String ipAddress) {
        try {
            UUID userId = jwtService.extractUserId(token);
            String username = jwtService.extractUsername(token);
            String resource = extractResourceFromToken(token);
            auditService.logAuthorizationDenied(
                userId,
                username,
                resource,
                ipAddress
            );
        } catch (Exception e) {
            // Cannot extract user info from token; log a generic denial
            logger.debug(
                "Could not extract user info from invalid token for audit logging"
            );
        }
    }

    private static String extractResourceFromToken(String token) {
        // The resource path isn't in the token, so we'll use a generic placeholder
        return "unknown";
    }

    private static String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    public static class JwtAuthenticationDetails {

        private final UUID userId;
        private final String username;
        private final String role;

        public JwtAuthenticationDetails(
            UUID userId,
            String username,
            String role
        ) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }

        public UUID getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }
    }
}
