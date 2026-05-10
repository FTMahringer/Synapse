package dev.synapse.core.users;

import dev.synapse.core.domain.User;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import dev.synapse.core.infrastructure.exception.ValidationException;
import dev.synapse.core.infrastructure.logging.LogCategory;
import dev.synapse.core.infrastructure.logging.LogLevel;
import dev.synapse.core.infrastructure.logging.SystemLogService;
import dev.synapse.core.repository.UserRepository;
import dev.synapse.core.infrastructure.security.PasswordHashingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHashingService passwordHashingService;
    private final SystemLogService logService;

    public UserService(
        UserRepository userRepository,
        PasswordHashingService passwordHashingService,
        SystemLogService logService
    ) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.logService = logService;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

    @Transactional
    public User create(User user, String plainPassword) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ValidationException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ValidationException("Email already exists: " + user.getEmail());
        }
        
        String hashedPassword = passwordHashingService.hash(plainPassword);
        user.setPasswordHash(hashedPassword);
        
        User saved = userRepository.save(user);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.AUTH,
            Map.of("component", "UserService", "userId", saved.getId().toString()),
            "USER_CREATED",
            Map.of("username", saved.getUsername(), "role", saved.getRole().name()),
            null,
            null
        );
        
        return saved;
    }

    @Transactional
    public User update(UUID id, User updates) {
        User existing = findById(id);
        
        boolean changed = false;
        Map<String, Object> changes = new java.util.HashMap<>();
        
        if (updates.getUsername() != null && !updates.getUsername().equals(existing.getUsername())) {
            if (userRepository.existsByUsername(updates.getUsername())) {
                throw new ValidationException("Username already exists: " + updates.getUsername());
            }
            changes.put("username", Map.of("from", existing.getUsername(), "to", updates.getUsername()));
            existing.setUsername(updates.getUsername());
            changed = true;
        }
        
        if (updates.getEmail() != null && !updates.getEmail().equals(existing.getEmail())) {
            if (userRepository.existsByEmail(updates.getEmail())) {
                throw new ValidationException("Email already exists: " + updates.getEmail());
            }
            changes.put("email", Map.of("from", existing.getEmail(), "to", updates.getEmail()));
            existing.setEmail(updates.getEmail());
            changed = true;
        }
        
        if (updates.getRole() != null && updates.getRole() != existing.getRole()) {
            changes.put("role", Map.of("from", existing.getRole().name(), "to", updates.getRole().name()));
            existing.setRole(updates.getRole());
            changed = true;
        }
        
        if (!changed) {
            return existing;
        }
        
        User saved = userRepository.save(existing);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.AUTH,
            Map.of("component", "UserService", "userId", id.toString()),
            "USER_UPDATED",
            changes,
            null,
            null
        );
        
        return saved;
    }

    @Transactional
    public void updatePassword(UUID id, String newPlainPassword) {
        User user = findById(id);
        String hashedPassword = passwordHashingService.hash(newPlainPassword);
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.AUTH,
            Map.of("component", "UserService", "userId", id.toString()),
            "PASSWORD_UPDATED",
            Map.of(),
            null,
            null
        );
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id.toString());
        }
        
        userRepository.deleteById(id);
        
        logService.log(
            LogLevel.INFO,
            LogCategory.AUTH,
            Map.of("component", "UserService", "userId", id.toString()),
            "USER_DELETED",
            Map.of(),
            null,
            null
        );
    }
}
