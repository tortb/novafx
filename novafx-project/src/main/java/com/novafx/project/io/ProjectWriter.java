package com.novafx.project.io;

import com.novafx.project.model.Project;

import java.nio.file.Path;

/**
 * Writes a NovaFX project to a {@code .nfx} file.
 * <p>
 * Implementations serialize the file-format {@link Project} model to
 * TOML and write it to the specified path.
 */
@FunctionalInterface
public interface ProjectWriter {

    /**
     * Writes a project to the specified path.
     * <p>
     * Creates the file if absent, overwrites if present.
     *
     * @param project the project to write; must not be null
     * @param path    the target file path; must not be null
     */
    void write(Project project, Path path);
}
