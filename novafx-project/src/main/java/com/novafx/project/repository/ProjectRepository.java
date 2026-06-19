package com.novafx.project.repository;

import com.novafx.core.domain.Project;

import java.nio.file.Path;
import java.util.Map;

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
     * Loads a domain project together with its persisted parameter values.
     * <p>
     * The default implementation delegates to {@link #load} and returns
     * an empty parameter map.  Override to read parameters from the file.
     *
     * @param path the path to the .nfx file; must not be null
     * @return a load result containing the domain project and parameters
     */
    default ProjectLoadResult loadWithParameters(Path path) {
        return new ProjectLoadResult(load(path), Map.of());
    }

    /**
     * Saves a domain project to the specified path.
     * <p>
     * Creates the file if absent, overwrites if present.
     *
     * @param project the domain project to save; must not be null
     * @param path    the target file path (should end in {@code .nfx})
     */
    void save(Project project, Path path);

    /**
     * Saves a domain project <em>with</em> parameter values to the
     * specified path.
     * <p>
     * The default implementation ignores parameters and delegates to
     * {@link #save(Project, Path)}.  Override to persist parameter values
     * in the {@code [parameter]} section of the TOML file.
     *
     * @param project    the domain project to save; must not be null
     * @param path       the target file path (should end in {@code .nfx})
     * @param parameters parameter name → value bindings to persist
     */
    default void save(Project project, Path path, Map<String, Double> parameters) {
        save(project, path);
    }
}
