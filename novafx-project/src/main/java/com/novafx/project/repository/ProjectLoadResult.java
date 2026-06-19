package com.novafx.project.repository;

import com.novafx.core.domain.Project;

import java.util.Map;

/**
 * The result of loading a project from a {@code .nfx} file,
 * including both the domain aggregate and the persisted parameter values.
 *
 * @param project    the loaded domain project
 * @param parameters parameter name → value bindings persisted in the file
 *                   (empty map when the file has no [parameter] section)
 */
public record ProjectLoadResult(Project project, Map<String, Double> parameters) {

    public ProjectLoadResult {
        if (parameters == null) parameters = Map.of();
    }

    /**
     * Returns {@code true} when the file contained persisted parameter values.
     */
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }
}
