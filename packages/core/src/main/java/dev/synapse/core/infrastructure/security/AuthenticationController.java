package dev.synapse.core.infrastructure.security;

import dev.synapse.core.dto.LoginRequest;
import dev.synapse.core.dto.RefreshTokenRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public AuthenticationService.AuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request.username(), request.password());
    }

    @PostMapping("/refresh")
    public AuthenticationService.AuthenticationResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authenticationService.refreshToken(request.refreshToken());
    }
}
