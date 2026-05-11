package dev.synapse.core.infrastructure.config;

import dev.synapse.core.infrastructure.security.JwtAuthenticationFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
        // Configure CSRF protection with CookieCsrfTokenRepository
        CsrfTokenRequestAttributeHandler requestHandler =
            new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
            // Enable CSRF protection with cookie-based token repository
            // This protects against CSRF attacks while supporting SPA architecture
            .csrf(csrf ->
                csrf
                    .csrfTokenRepository(
                        CookieCsrfTokenRepository.withHttpOnlyFalse()
                    )
                    .csrfTokenRequestHandler(requestHandler)
                    // Exempt stateless public endpoints from CSRF
                    .ignoringRequestMatchers(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/health",
                        "/actuator/**"
                    )
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth ->
                auth
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/api/health")
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            )
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<Filter> securityHeadersFilter() {
        FilterRegistrationBean<Filter> registration =
            new FilterRegistrationBean<>();
        registration.setFilter((request, response, chain) -> {
            jakarta.servlet.http.HttpServletResponse httpResponse =
                (jakarta.servlet.http.HttpServletResponse) response;
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader(
                "Strict-Transport-Security",
                "max-age=31536000; includeSubDomains"
            );
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader(
                "Referrer-Policy",
                "strict-origin-when-cross-origin"
            );
            httpResponse.setHeader(
                "Permissions-Policy",
                "geolocation=(), microphone=(), camera=()"
            );
            chain.doFilter(request, response);
        });
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MIN_VALUE);
        return registration;
    }
}
