package com.novafx.project;

/**
 * Thrown when a NovaFX project file ({@code .nfx}) or compiled file
 * ({@code .nfxc}) cannot be read because of format errors.
 * <p>
 * Covers: illegal TOML syntax, missing version field, corrupted NFXC header,
 * unknown version numbers, and any structural violation of the file format.
 */
public class ProjectFormatException extends RuntimeException {

    /**
     * Constructs a new exception with a detail message.
     *
     * @param message the detail message
     */
    public ProjectFormatException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with a detail message and cause.
     *
     * @param message the detail message
     * @param cause   the root cause
     */
    public ProjectFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
