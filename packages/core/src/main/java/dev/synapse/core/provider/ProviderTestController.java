package dev.synapse.core.provider;

import dev.synapse.core.dto.TestProviderRequest;
import dev.synapse.core.dto.TestProviderResponse;
import dev.synapse.core.service.ProviderTestService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/providers")
public class ProviderTestController {

    private final ProviderTestService testService;

    public ProviderTestController(ProviderTestService testService) {
        this.testService = testService;
    }

    @PostMapping("/test")
    public TestProviderResponse testProvider(@Valid @RequestBody TestProviderRequest request) {
        return testService.testProvider(request);
    }
}
