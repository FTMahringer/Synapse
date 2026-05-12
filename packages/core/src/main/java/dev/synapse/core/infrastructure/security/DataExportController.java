package dev.synapse.core.infrastructure.security;

import dev.synapse.core.common.domain.User;
import dev.synapse.core.common.repository.UserRepository;
import dev.synapse.core.infrastructure.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/compliance")
public class DataExportController {

    private final DataExportService dataExportService;
    private final DataDeletionService dataDeletionService;
    private final UserRepository userRepository;

    public DataExportController(
        DataExportService dataExportService,
        DataDeletionService dataDeletionService,
        UserRepository userRepository
    ) {
        this.dataExportService = dataExportService;
        this.dataDeletionService = dataDeletionService;
        this.userRepository = userRepository;
    }

    @GetMapping(value = "/export/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportUserData(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        String jsonExport = dataExportService.exportUserDataAsJson(
            userId,
            user.getUsername(),
            user.getEmail(),
            user.getRole().name(),
            user.getCreatedAt()
        );

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonExport);
    }

    @PostMapping("/anonymize/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void anonymizeUserData(@PathVariable UUID userId) {
        dataDeletionService.anonymizeUserData(userId);
    }

    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserData(@PathVariable UUID userId) {
        dataDeletionService.deleteUserData(userId);
    }
}
