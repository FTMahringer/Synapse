package dev.synapse.users;

import dev.synapse.core.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> listUsers() {
        return userService.findAll().stream()
            .map(DtoMapper::toDTO)
            .toList();
    }

    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable UUID id) {
        return DtoMapper.toDTO(userService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@Valid @RequestBody CreateUserRequest request) {
        var user = DtoMapper.fromCreateRequest(request);
        var created = userService.create(user, request.password());
        return DtoMapper.toDTO(created);
    }

    @PatchMapping("/{id}")
    public UserDTO updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        var existing = userService.findById(id);
        
        if (request.username() != null) {
            existing.setUsername(request.username());
        }
        if (request.email() != null) {
            existing.setEmail(request.email());
        }
        if (request.role() != null) {
            existing.setRole(dev.synapse.core.common.domain.User.UserRole.valueOf(request.role()));
        }
        
        var updated = userService.update(id, existing);
        return DtoMapper.toDTO(updated);
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(@PathVariable UUID id, @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(id, request.newPassword());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteById(id);
    }
}
