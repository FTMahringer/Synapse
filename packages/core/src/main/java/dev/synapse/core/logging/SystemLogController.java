package dev.synapse.core.logging;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class SystemLogController {

    private final SystemLogService logs;

    public SystemLogController(SystemLogService logs) {
        this.logs = logs;
    }

    @GetMapping
    public List<SystemLog> latest(@RequestParam(defaultValue = "100") int limit) {
        return logs.latest(limit);
    }
}
