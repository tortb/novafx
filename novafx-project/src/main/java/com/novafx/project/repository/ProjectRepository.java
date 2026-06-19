package com.novafx.project.repository;

import com.novafx.core.domain.Project;

import java.nio.file.Path;

/**
 * Repository for loading and saving NovaFX domain projects
 * to the filesystem.
 * <p>
 * Uses {@code .nfx} (TOML) for human-readable project files.
 * Internally delegates to the IO layer and handles conversion
 * between the file-format model and the domain aggregate.
 */
public interface ProjectRepository {

    /**
     * Loads a domain project from a {@code .nfx} file.
     *
     * @param path the path to the .nfx file; must not be null
     * @return the loaded domain project
     * @throws com.novafx.project.ProjectFormatException if the file
     *         is malformed or cannot be read
     */
    Project load(Path path);

    /**
     * Saves a domain project to the specified path.
     * <p>
     * Creates the file if absent, overwrites if present.
     *
     * @param project the domain project to save; must not be null
     * @param path    the target file path (should end in {@code .nfx})
     */
    void save(Project project, Path path);
}
