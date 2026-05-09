package dev.synapse.core.plugin;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validates plugin manifests before install.
 * Checks required fields, type enum, and version format.
 */
@Component
public class ManifestValidator {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+\\.\\d+.*$");
    private static final Pattern ID_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9\\-]{0,63}$");
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    public ValidationResult validate(Map<String, Object> raw) {
        return validate(PluginManifest.fromMap(raw));
    }

    public ValidationResult validate(PluginManifest manifest) {
        List<String> errors = new ArrayList<>();

        if (blank(manifest.id())) {
            errors.add("id is required");
        } else if (!ID_PATTERN.matcher(manifest.id()).matches()) {
            errors.add("id must be lowercase alphanumeric with hyphens, max 64 chars");
        }

        if (blank(manifest.name())) {
            errors.add("name is required");
        }

        if (manifest.type() == null) {
            errors.add("type is required and must be one of: CHANNEL, MODEL, SKILL, MCP");
        }

        if (blank(manifest.version())) {
            errors.add("version is required");
        } else if (!VERSION_PATTERN.matcher(manifest.version()).matches()) {
            errors.add("version must follow semver format (e.g. 1.0.0)");
        }

        if (blank(manifest.author())) {
            errors.add("author is required");
        }

        if (manifest.description() != null && manifest.description().length() > MAX_DESCRIPTION_LENGTH) {
            errors.add("description must be <= " + MAX_DESCRIPTION_LENGTH + " characters");
        }

        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
