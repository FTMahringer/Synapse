package dev.synapse.core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
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
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);
                        
                        authentication.setDetails(new JwtAuthenticationDetails(userId, username, role));
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                // Invalid token, continue without authentication
            }
        }
        
        filterChain.doFilter(request, response);
    }

    public static class JwtAuthenticationDetails {
        private final UUID userId;
        private final String username;
        private final String role;

        public JwtAuthenticationDetails(UUID userId, String username, String role) {
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
