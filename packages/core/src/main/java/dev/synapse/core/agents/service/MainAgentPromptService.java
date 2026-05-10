package dev.synapse.core.agents.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class MainAgentPromptService {

    private static final String AGENT_DIR = "agents/main/";

    public String assemblePrompt() {
        StringBuilder prompt = new StringBuilder();

        prompt.append(readFile("system-prompt.md")).append("\n\n");
        prompt.append("# Identity\n").append(readFile("identity.md")).append("\n\n");
        prompt.append("# Soul\n").append(readFile("soul.md")).append("\n\n");
        prompt.append("# Connections\n").append(readFile("connections.md")).append("\n\n");

        return prompt.toString();
    }

    private String readFile(String filename) {
        try {
            Resource resource = new ClassPathResource(AGENT_DIR + filename);
            return FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            return ""; // Return empty if file doesn't exist
        }
    }
}
