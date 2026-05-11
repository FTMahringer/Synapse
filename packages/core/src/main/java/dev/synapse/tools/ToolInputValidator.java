package dev.synapse.tools;

import dev.synapse.core.infrastructure.exception.ValidationException;

import java.util.Map;

public final class ToolInputValidator {

    private ToolInputValidator() {}

    public static Map<String, Object> requireObject(Map<String, Object> root, String fieldName) {
        Object value = root.get(fieldName);
        if (!(value instanceof Map<?, ?> rawMap)) {
            throw new ValidationException("Field '" + fieldName + "' must be an object");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> castMap = (Map<String, Object>) rawMap;
        return castMap;
    }

    public static boolean optionalBoolean(Map<String, Object> root, String fieldName, boolean defaultValue) {
        Object value = root.get(fieldName);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        throw new ValidationException("Field '" + fieldName + "' must be a boolean");
    }

    public static String optionalString(Map<String, Object> root, String fieldName) {
        Object value = root.get(fieldName);
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        throw new ValidationException("Field '" + fieldName + "' must be a string");
    }
}
