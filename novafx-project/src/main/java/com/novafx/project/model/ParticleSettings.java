package com.novafx.project.model;

/**
 * Particle rendering settings in a NovaFX project file ({@code .nfx}).
 *
 * @param size    particle size (must be positive)
 * @param density particle density (must be positive)
 */
public record ParticleSettings(double size, double density) {

    /** Default particle size. */
    public static final double DEFAULT_SIZE = 0.1;

    /** Default particle density. */
    public static final double DEFAULT_DENSITY = 1.0;

    /**
     * Constructs a ParticleSettings record with default values if
     * the given values are non-positive.
     */
    public ParticleSettings {
        if (size <= 0) size = DEFAULT_SIZE;
        if (density <= 0) density = DEFAULT_DENSITY;
    }

    /** Creates settings with default values. */
    public static ParticleSettings defaults() {
        return new ParticleSettings(DEFAULT_SIZE, DEFAULT_DENSITY);
    }
}
