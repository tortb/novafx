package com.novafx.project;

import com.novafx.core.domain.Project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for persisting and loading {@link Project} aggregates.
 * <p>
 * Projects are stored as {@code .nfx} files in the workspace directory.
 * The concrete file format is an implementation detail of the repository.
 */
public interface ProjectRepository {

    /**
     * Saves a project. Creates the file if absent, overwrites if present.
     *
     * @param project the project to save; must not be null
     */
    void save(Project project);

    /**
     * Loads a project by its unique identifier.
     *
     * @param id the project ID; must not be null
     * @return an Optional containing the project, or empty if not found
     */
    Optional<Project> findById(UUID id);

    /**
     * Lists all projects in the workspace.
     *
     * @return immutable list of all known projects (never null)
     */
    List<Project> findAll();

    /**
     * Deletes a project by its unique identifier.
     *
     * @param id the project ID; must not be null
     * @return true if a project was deleted, false otherwise
     */
    boolean deleteById(UUID id);

    /**
     * Returns the total number of projects in the workspace.
     *
     * @return project count
     */
    long count();
}
