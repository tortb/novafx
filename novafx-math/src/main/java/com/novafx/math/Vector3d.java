package com.novafx.math;

/**
 * A 3-dimensional vector with {@code double} precision components.
 * Immutable value object used throughout the rendering and sampling pipeline.
 *
 * @param x the x-coordinate
 * @param y the y-coordinate
 * @param z the z-coordinate
 */
public record Vector3d(double x, double y, double z) {

    /**
     * Returns the Euclidean (L2) norm of this vector.
     *
     * @return sqrt(x² + y² + z²)
     */
    public double norm() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Returns a new vector with each component multiplied by {@code scalar}.
     *
     * @param scalar the scaling factor
     * @return scaled vector
     */
    public Vector3d multiply(double scalar) {
        return new Vector3d(x * scalar, y * scalar, z * scalar);
    }

    /**
     * Returns a new vector that is the sum of this vector and {@code other}.
     *
     * @param other vector to add
     * @return element-wise sum
     */
    public Vector3d add(Vector3d other) {
        return new Vector3d(x + other.x, y + other.y, z + other.z);
    }

    /**
     * Returns a new vector that is the difference of this vector and {@code other}.
     *
     * @param other vector to subtract
     * @return element-wise difference
     */
    public Vector3d subtract(Vector3d other) {
        return new Vector3d(x - other.x, y - other.y, z - other.z);
    }

    /**
     * The zero vector (0, 0, 0).
     */
    public static final Vector3d ZERO = new Vector3d(0, 0, 0);
}
