package com.novafx.core.domain;

import com.novafx.math.FunctionDefinition;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root representing a NovaFX project.
 * <p>
 * A project holds metadata together with its parametric function definition.
 * It is persisted to and loaded from {@code .nfx} files.
 */
public final class Project {

    private final UUID id;
    private final String name;
    private final String description;
    private final FunctionDefinition functionDefinition;
    private final Instant createdAt;
    private final Instant updatedAt;

    /**
     * Constructs a new project.
     *
     * @param id                 unique identifier; must not be null
     * @param name               human-readable name; must not be blank
     * @param description        optional description (may be empty)
     * @param functionDefinition the parametric function; must not be null
     * @param createdAt          creation timestamp; must not be null
     * @param updatedAt          last-update timestamp; must not be null
     */
    public Project(UUID id, String name, String description,
                   FunctionDefinition functionDefinition,
                   Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "");
        this.functionDefinition = Objects.requireNonNull(functionDefinition,
                "functionDefinition must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    /** Unique identifier of this project. */
    public UUID id() {
        return id;
    }

    /** Human-readable name. */
    public String name() {
        return name;
    }

    /** Optional description (may be empty, never null). */
    public String description() {
        return description;
    }

    /** The parametric function definition. */
    public FunctionDefinition functionDefinition() {
        return functionDefinition;
    }

    /** Timestamp of project creation. */
    public Instant createdAt() {
        return createdAt;
    }

    /** Timestamp of the last modification. */
    public Instant updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project project)) return false;
        return id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Project{id=" + id + ", name='" + name + "'}";
    }
}
