package com.novafx.core.domain;

import com.novafx.math.FunctionDefinition;

import java.util.Objects;

/**
 * A named preset that combines a human-readable label with a
 * {@link FunctionDefinition}.
 * <p>
 * Presets provide quick access to well-known parametric shapes
 * and are displayed in the UI preset panel.
 */
public final class Preset {

    private final String name;
    private final FunctionDefinition definition;

    /**
     * Constructs a preset.
     *
     * @param name       display name; must not be blank
     * @param definition the parametric function; must not be null
     */
    public Preset(String name, FunctionDefinition definition) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        this.name = name;
        this.definition = Objects.requireNonNull(definition, "definition must not be null");
    }

    /** Display name of this preset. */
    public String name() {
        return name;
    }

    /** The parametric function definition. */
    public FunctionDefinition definition() {
        return definition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Preset preset)) return false;
        return name.equals(preset.name) && definition.equals(preset.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, definition);
    }

    @Override
    public String toString() {
        return "Preset{name='" + name + "'}";
    }
}
