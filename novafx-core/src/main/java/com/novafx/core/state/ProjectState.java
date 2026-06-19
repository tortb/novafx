package com.novafx.core.state;

import com.novafx.core.domain.Project;
import com.novafx.function.Parameter;
import com.novafx.math.Vector3d;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of the entire active-project state — the single
 * source of truth consumed by every UI panel.
 * <p>
 * Every mutating operation returns a <em>new</em> instance via one of
 * the {@code withXxx(...)} methods, keeping the old state intact until
 * the caller explicitly replaces it (copy-on-write).
 * <p>
 * Fields:
 * <ul>
 *   <li>{@code project} — the domain aggregate (never null)</li>
 *   <li>{@code projectPath} — file path, {@code null} when unsaved</li>
 *   <li>{@code points} — the current sampled point cloud (immutable)</li>
 *   <li>{@code parameters} — current parameter name → value map (immutable)</li>
 * </ul>
 *
 * @param project      the domain aggregate; must not be null
 * @param projectPath  optional file path ({@code null} when unsaved)
 * @param points       immutable list of sampled 3D points
 * @param parameters   immutable map of current parameter values
 */
public record ProjectState(
        Project project,
        Path projectPath,
        List<Vector3d> points,
        Map<String, Parameter> parameters
) {

    public ProjectState {
        // Defensive copies guarantee immutability of the collections
        points = List.copyOf(points);
        parameters = Map.copyOf(parameters);
    }

    // ---------------------------------------------------------------
    //  Query helpers
    // ---------------------------------------------------------------

    /** Shortcut for {@code project.functionDefinition()}. */
    public com.novafx.math.FunctionDefinition functionDefinition() {
        return project.functionDefinition();
    }

    /** True when this project has never been saved to disk. */
    public boolean isUnsaved() {
        return projectPath == null;
    }

    // ---------------------------------------------------------------
    //  Copy-on-write builders
    // ---------------------------------------------------------------

    /** Returns a new state with a different project (path / points / params
     *  carried over unchanged — caller must update them if needed). */
    public ProjectState withProject(Project project) {
        return new ProjectState(project, projectPath, points, parameters);
    }

    public ProjectState withPath(Path projectPath) {
        return new ProjectState(project, projectPath, points, parameters);
    }

    public ProjectState withPoints(List<Vector3d> points) {
        return new ProjectState(project, projectPath, points, parameters);
    }

    public ProjectState withParameters(Map<String, Parameter> parameters) {
        return new ProjectState(project, projectPath, points, parameters);
    }
}
