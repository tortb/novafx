package com.novafx.project.migration;

import com.novafx.project.model.Project;

/**
 * A single migration step that transforms a project from one version
 * to the next.
 * <p>
 * Implementations are registered in {@link VersionMigrator} and applied
 * in ascending version order until the project reaches the current version.
 */
public interface MigrationStep {

    /**
     * Returns the source version that this step handles (e.g. {@code "1.0"}.
     * The step's {@link #migrate} is called when the project version
     * equals this string.
     */
    String sourceVersion();

    /**
     * Migrates the project one version forward.
     *
     * @param project the project at {@link #sourceVersion()}
     * @return a new project at the next version
     */
    Project migrate(Project project);
}
