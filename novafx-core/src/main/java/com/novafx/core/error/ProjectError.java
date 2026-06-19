package com.novafx.core.error;

import java.util.Optional;

/**
 * A structured error or warning produced during project loading,
 * validation, compilation, or migration.
 *
 * @param level     severity (ERROR / WARNING / INFO)
 * @param code      categorized error code
 * @param message   human-readable description (already localized where applicable)
 * @param source    the TOML section, field, or pipeline stage that produced the error
 * @param line      line number in the source file (0 if unknown)
 * @param column    column number in the source file (0 if unknown)
 */
public record ProjectError(
        ErrorLevel level,
        ErrorCode code,
        String message,
        String source,
        int line,
        int column
) {

    public ProjectError {
        if (line < 0) line = 0;
        if (column < 0) column = 0;
    }

    // ── Convenience constructors ──

    public ProjectError(ErrorLevel level, ErrorCode code, String message) {
        this(level, code, message, null, 0, 0);
    }

    public ProjectError(ErrorLevel level, ErrorCode code, String message, String source) {
        this(level, code, message, source, 0, 0);
    }

    // ── Queries ──

    public boolean isError() {
        return level == ErrorLevel.ERROR;
    }

    public boolean isWarning() {
        return level == ErrorLevel.WARNING;
    }

    public Optional<String> sourceSection() {
        return Optional.ofNullable(source);
    }

    /**
     * Returns a single-line representation suitable for log output or
     * status-bar display.
     */
    public String formatted() {
        var sb = new StringBuilder();
        sb.append('[').append(level).append("] ");
        sb.append(message);
        if (source != null) {
            sb.append(" — ").append(source);
            if (line > 0) {
                sb.append(':').append(line);
                if (column > 0) sb.append(':').append(column);
            }
        }
        return sb.toString();
    }
}
