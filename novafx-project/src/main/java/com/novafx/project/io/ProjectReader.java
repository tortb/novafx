package com.novafx.project.io;

import com.novafx.project.model.Project;

import java.nio.file.Path;

/**
 * Reads a NovaFX project from a {@code .nfx} file.
 * <p>
 * Implementations parse the TOML content and return a file-format
 * {@link Project} model. No business logic is applied.
 */
@FunctionalInterface
public interface ProjectReader {

    /**
     * Reads and parses a {@code .nfx} file.
     *
     * @param path the path to the .nfx file; must not be null
     * @return the parsed project model
     * @throws com.novafx.project.ProjectFormatException if the file is
     *         malformed, uses an unknown version, or cannot be read
     */
    Project read(Path path);
}
