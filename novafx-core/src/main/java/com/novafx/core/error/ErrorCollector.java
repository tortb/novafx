package com.novafx.core.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Accumulates {@link ProjectError} instances during a pipeline run.
 * <p>
 * Callers add errors as they are discovered.  After the pipeline stage
 * completes, call {@link #hasErrors()} to decide whether to abort, and
 * {@link #getErrors()} to surface diagnostics to the user.
 * <p>
 * This class is <em>not</em> thread-safe — each pipeline run gets its own
 * collector.
 */
public final class ErrorCollector {

    private final List<ProjectError> errors = new ArrayList<>();

    // ── Adding errors ──

    public void add(ProjectError error) {
        errors.add(Objects.requireNonNull(error, "error must not be null"));
    }

    public void addError(ErrorCode code, String message) {
        errors.add(new ProjectError(ErrorLevel.ERROR, code, message));
    }

    public void addError(ErrorCode code, String message, String source) {
        errors.add(new ProjectError(ErrorLevel.ERROR, code, message, source));
    }

    public void addError(ErrorCode code, String message, String source, int line, int column) {
        errors.add(new ProjectError(ErrorLevel.ERROR, code, message, source, line, column));
    }

    public void addWarning(ErrorCode code, String message) {
        errors.add(new ProjectError(ErrorLevel.WARNING, code, message));
    }

    public void addWarning(ErrorCode code, String message, String source) {
        errors.add(new ProjectError(ErrorLevel.WARNING, code, message, source));
    }

    public void addInfo(ErrorCode code, String message) {
        errors.add(new ProjectError(ErrorLevel.INFO, code, message));
    }

    public void addInfo(ErrorCode code, String message, String source) {
        errors.add(new ProjectError(ErrorLevel.INFO, code, message, source));
    }

    // ── Queries ──

    /**
     * Returns {@code true} when at least one {@link ErrorLevel#ERROR} has
     * been collected — the pipeline should abort.
     */
    public boolean hasErrors() {
        return errors.stream().anyMatch(ProjectError::isError);
    }

    /**
     * Returns {@code true} when at least one {@link ErrorLevel#WARNING} has
     * been collected.
     */
    public boolean hasWarnings() {
        return errors.stream().anyMatch(ProjectError::isWarning);
    }

    /**
     * Returns an unmodifiable view of all collected errors, in insertion order.
     */
    public List<ProjectError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Returns errors filtered by severity level.
     */
    public List<ProjectError> getErrors(ErrorLevel level) {
        return errors.stream()
                .filter(e -> e.level() == level)
                .toList();
    }

    /**
     * Returns the total number of collected errors (all levels).
     */
    public int size() {
        return errors.size();
    }

    /**
     * Returns {@code true} when no errors have been collected.
     */
    public boolean isEmpty() {
        return errors.isEmpty();
    }

    /**
     * Merges all errors from {@code other} into this collector.
     */
    public void merge(ErrorCollector other) {
        errors.addAll(other.errors);
    }

    /**
     * Clears all accumulated errors.  Useful when re-using a collector.
     */
    public void clear() {
        errors.clear();
    }
}
