package dev.synapse.core.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Integer.MIN_VALUE + 1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final int requestsPerMinute;
    private final int loginRequestsPerMinute;
    private final Cache<String, AtomicInteger> requestCounts;

    public RateLimitingFilter(
        @Value("${rate-limiting.requests-per-minute:60}") int requestsPerMinute,
        @Value(
            "${rate-limiting.login-requests-per-minute:10}"
        ) int loginRequestsPerMinute
    ) {
        this.requestsPerMinute = requestsPerMinute;
        this.loginRequestsPerMinute = loginRequestsPerMinute;
        this.requestCounts = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return (
            path.startsWith("/actuator/") ||
            path.startsWith("/api/health") ||
            path.equals("/api/health") ||
            path.startsWith("/static/") ||
            path.equals("/favicon.ico")
        );
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        int limit = isLoginRequest(request)
            ? loginRequestsPerMinute
            : requestsPerMinute;

        AtomicInteger counter = requestCounts.get(clientIp, k ->
            new AtomicInteger(0)
        );
        int currentCount = counter.incrementAndGet();

        if (currentCount > limit) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response
                .getWriter()
                .write(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}"
                );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "/api/auth/login".equals(request.getRequestURI());
    }
}
