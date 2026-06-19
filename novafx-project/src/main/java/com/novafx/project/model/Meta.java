package com.novafx.project.model;

import java.util.Objects;

/**
 * Metadata section of a NovaFX project file ({@code .nfx}).
 * <p>
 * Contains human-readable identification such as the project name
 * and author name.
 *
 * @param name   the project name (must not be blank)
 * @param author the author name (may be empty)
 */
public record Meta(String name, String author) {

    /**
     * Constructs a Meta record.
     *
     * @param name   the project name; if null or blank defaults to "Untitled"
     * @param author the author name; defaults to empty string if null
     */
    public Meta {
        if (name == null || name.isBlank()) {
            name = "Untitled";
        }
        author = Objects.requireNonNullElse(author, "");
    }
}
