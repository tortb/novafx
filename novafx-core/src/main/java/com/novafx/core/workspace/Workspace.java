package com.novafx.core.workspace;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * A NovaFX workspace manages an ordered collection of
 * {@link ProjectTreeModel project trees}.
 * <p>
 * Conceptually, a workspace corresponds to a directory containing
 * one or more {@code .nfx} files.  The workspace is the single
 * source of truth for "which projects are open" and is consumed
 * directly by the {@code ProjectExplorer} UI panel.
 * <p>
 * This class supports:
 * <ul>
 *   <li>Adding / removing projects</li>
 *   <li>Looking up a tree model by project id or name</li>
 *   <li>Listening for structural changes (open, close)</li>
 *   <li>Empty-state detection ({@link #isEmpty()})</li>
 * </ul>
 * <p>
 * Thread-safety: this class is <em>not</em> thread-safe.
 * All mutation must happen on the JavaFX application thread.
 */
public final class Workspace {

    private final List<ProjectTreeModel> projects = new ArrayList<>();
    private Consumer<ProjectTreeModel> onProjectAdded;
    private Runnable onProjectRemoved;

    // ---------------------------------------------------------------
    //  Query
    // ---------------------------------------------------------------

    /**
     * Returns an immutable snapshot of the currently open projects.
     */
    public List<ProjectTreeModel> getProjects() {
        return List.copyOf(projects);
    }

    /**
     * Returns the number of open projects.
     */
    public int size() {
        return projects.size();
    }

    /**
     * Returns {@code true} when no projects are open.
     */
    public boolean isEmpty() {
        return projects.isEmpty();
    }

    /**
     * Looks up a tree model by its domain project id.
     *
     * @param projectId the {@link java.util.UUID} string
     * @return the matching model, or empty
     */
    public Optional<ProjectTreeModel> findById(String projectId) {
        Objects.requireNonNull(projectId, "projectId must not be null");
        return projects.stream()
                .filter(m -> m.project().id().toString().equals(projectId))
                .findFirst();
    }

    /**
     * Looks up a tree model by project name (first match).
     *
     * @param name the project name (case-sensitive)
     * @return the matching model, or empty
     */
    public Optional<ProjectTreeModel> findByName(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return projects.stream()
                .filter(m -> m.project().name().equals(name))
                .findFirst();
    }

    // ---------------------------------------------------------------
    //  Mutation
    // ---------------------------------------------------------------

    /**
     * Adds a project tree model to the workspace.
     *
     * @param model the model to add (must not be null)
     * @throws IllegalArgumentException if a project with the same id
     *                                  is already open
     */
    public void addProject(ProjectTreeModel model) {
        Objects.requireNonNull(model, "model must not be null");

        String id = model.project().id().toString();
        if (findById(id).isPresent()) {
            throw new IllegalArgumentException(
                    "Project '" + model.project().name()
                            + "' is already open (id=" + id + ")");
        }

        projects.add(model);

        if (onProjectAdded != null) {
            onProjectAdded.accept(model);
        }
    }

    /**
     * Removes a project by its index.
     *
     * @param index the index of the project to remove
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public void removeProject(int index) {
        projects.remove(index);

        if (onProjectRemoved != null) {
            onProjectRemoved.run();
        }
    }

    /**
     * Removes a project by its domain project id.
     *
     * @param projectId the UUID string of the project to remove
     * @return {@code true} if a project was removed
     */
    public boolean removeProjectById(String projectId) {
        Optional<ProjectTreeModel> found = findById(projectId);
        if (found.isPresent()) {
            projects.remove(found.get());
            if (onProjectRemoved != null) {
                onProjectRemoved.run();
            }
            return true;
        }
        return false;
    }

    /**
     * Removes all projects from the workspace.
     */
    public void clear() {
        projects.clear();
        if (onProjectRemoved != null) {
            onProjectRemoved.run();
        }
    }

    // ---------------------------------------------------------------
    //  Tracking — reorder later when drag-and-drop lands
    // ---------------------------------------------------------------

    /**
     * Moves a project from one index to another (for drag-reorder).
     *
     * @param fromIndex current index
     * @param toIndex   target index
     */
    public void moveProject(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= projects.size()) return;
        if (toIndex < 0 || toIndex >= projects.size()) return;
        ProjectTreeModel model = projects.remove(fromIndex);
        projects.add(toIndex, model);
    }

    // ---------------------------------------------------------------
    //  Listeners
    // ---------------------------------------------------------------

    /**
     * Registers a callback invoked when a project is added.
     *
     * @param callback receives the newly-added model
     */
    public void setOnProjectAdded(Consumer<ProjectTreeModel> callback) {
        this.onProjectAdded = callback;
    }

    /**
     * Registers a callback invoked after a project is removed.
     */
    public void setOnProjectRemoved(Runnable callback) {
        this.onProjectRemoved = callback;
    }
}
