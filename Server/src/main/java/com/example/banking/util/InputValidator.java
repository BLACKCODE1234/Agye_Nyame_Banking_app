package com.example.banking.util;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class InputValidator {
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern PIN_PATTERN = Pattern.compile("^\\d{4}$");

    private InputValidator() {
    }

    public static String requireMobile(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (!MOBILE_PATTERN.matcher(value).matches()) {
            throw new ValidationException("Mobile number must be exactly 10 digits.");
        }
        return value;
    }

    public static String requirePin(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (!PIN_PATTERN.matcher(value).matches()) {
            throw new ValidationException("PIN must be exactly 4 digits.");
        }
        return value;
    }

    public static TextFormatter<String> digitsOnlyFormatter(int maxLength) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0," + maxLength + "}")) {
                return change;
            }
            return null;
        };
        return new TextFormatter<>(filter);
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
