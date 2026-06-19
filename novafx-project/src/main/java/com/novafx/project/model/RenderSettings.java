package com.novafx.project.model;

/**
 * Render configuration in a NovaFX project file ({@code .nfx}).
 *
 * @param grid whether to render the grid
 * @param axis whether to render the coordinate axes
 */
public record RenderSettings(boolean grid, boolean axis) {

    /** Creates settings with default values (grid=true, axis=true). */
    public static RenderSettings defaults() {
        return new RenderSettings(true, true);
    }
}
