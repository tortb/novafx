package com.novafx.project.model;

import com.novafx.math.FunctionDefinition;

import java.util.Objects;

/**
 * File-format representation of a NovaFX project ({@code .nfx}).
 * <p>
 * This is a pure data model that maps directly to the TOML file structure.
 * It is NOT the in-memory domain aggregate — use {@code com.novafx.core.domain.Project}
 * for that purpose.
 * <p>
 * Fields:
 * <ul>
 *   <li>{@code version} — file format version (e.g. "1.0")</li>
 *   <li>{@code meta} — project metadata (name, author)</li>
 *   <li>{@code function} — parametric function definition</li>
 *   <li>{@code particle} — particle rendering settings</li>
 *   <li>{@code render} — grid/axis visibility settings</li>
 * </ul>
 *
 * @param version  file format version; must not be null or blank
 * @param meta     project metadata
 * @param function the parametric function definition; must not be null
 * @param particle particle settings; must not be null
 * @param render   render settings; must not be null
 */
public record Project(
        String version,
        Meta meta,
        FunctionDefinition function,
        ParticleSettings particle,
        RenderSettings render
) {

    /** The current file format version. */
    public static final String CURRENT_VERSION = "1.0";

    /**
     * Constructs a Project with validation.
     */
    public Project {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("version must not be null or blank");
        }
        Objects.requireNonNull(function, "function must not be null");
        if (meta == null) meta = new Meta("Untitled", "");
        if (particle == null) particle = ParticleSettings.defaults();
        if (render == null) render = RenderSettings.defaults();
    }

    /**
     * Creates a minimal project with a function definition.
     *
     * @param function the parametric function
     * @return a new Project with default values for all other fields
     */
    public static Project from(FunctionDefinition function) {
        return new Project(CURRENT_VERSION,
                new Meta("Untitled", ""),
                function,
                ParticleSettings.defaults(),
                RenderSettings.defaults());
    }
}
