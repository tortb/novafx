package com.novafx.renderer;

/**
 * An orbit camera that rotates around a target point.
 * <p>
 * Supports:
 * <ul>
 *   <li>Rotation via azimuth (horizontal) and elevation (vertical) angles</li>
 *   <li>Zoom by adjusting distance from the target</li>
 *   <li>Pan by moving the target point</li>
 *   <li>Perspective and orthographic projection modes</li>
 * </ul>
 * Angles are in radians. All mutations return {@code this} for chaining.
 */
public final class Camera {

    /** Projection type: perspective (3D) or orthographic (2D). */
    public enum ProjectionType { PERSPECTIVE, ORTHOGRAPHIC }

    private float azimuth;
    private float elevation;
    private float distance;
    private float targetX;
    private float targetY;
    private float targetZ;
    private float fovDegrees;
    private float nearPlane;
    private float farPlane;
    private ProjectionType projectionType = ProjectionType.PERSPECTIVE;

    /** Creates a default camera at a 45-degree view of the origin. */
    public Camera() {
        this.azimuth = (float) Math.toRadians(45);
        this.elevation = (float) Math.toRadians(30);
        this.distance = 12f;
        this.targetX = 0;
        this.targetY = 0;
        this.targetZ = 0;
        this.fovDegrees = 60;
        this.nearPlane = 0.1f;
        this.farPlane = 1000f;
    }

    // ---- Projection type ----

    public ProjectionType projectionType() {
        return projectionType;
    }

    public Camera setProjectionType(ProjectionType type) {
        this.projectionType = type;
        return this;
    }

    // ---- Rotation ----

    /** Horizontal rotation angle in radians. */
    public float azimuth() {
        return azimuth;
    }

    /** Vertical rotation angle in radians (clamped to ±89 degrees). */
    public float elevation() {
        return elevation;
    }

    public Camera setAzimuth(float radians) {
        this.azimuth = radians;
        return this;
    }

    public Camera setElevation(float radians) {
        float maxElev = (float) Math.toRadians(89);
        this.elevation = Math.max(-maxElev, Math.min(maxElev, radians));
        return this;
    }

    /**
     * Adds to the azimuth angle.
     */
    public Camera rotateAzimuth(float deltaRadians) {
        this.azimuth += deltaRadians;
        return this;
    }

    /**
     * Adds to the elevation angle, clamping to ±89°.
     */
    public Camera rotateElevation(float deltaRadians) {
        float maxElev = (float) Math.toRadians(89);
        float newElev = this.elevation + deltaRadians;
        this.elevation = Math.max(-maxElev, Math.min(maxElev, newElev));
        return this;
    }

    // ---- Zoom ----

    /** Distance from the camera to the target point. */
    public float distance() {
        return distance;
    }

    public Camera setDistance(float d) {
        this.distance = Math.max(0.5f, d);
        return this;
    }

    /**
     * Adds to the distance (positive = zoom out).
     */
    public Camera zoom(float delta) {
        this.distance = Math.max(0.5f, this.distance + delta);
        return this;
    }

    // ---- Pan (target) ----

    /** X-coordinate of the orbit target. */
    public float targetX() {
        return targetX;
    }

    /** Y-coordinate of the orbit target. */
    public float targetY() {
        return targetY;
    }

    /** Z-coordinate of the orbit target. */
    public float targetZ() {
        return targetZ;
    }

    /**
     * Translates the orbit target (pan).
     */
    public Camera pan(float dx, float dy, float dz) {
        this.targetX += dx;
        this.targetY += dy;
        this.targetZ += dz;
        return this;
    }

    // ---- Projection parameters ----

    /** Vertical field of view in degrees. */
    public float fovDegrees() {
        return fovDegrees;
    }

    /** Near clipping plane distance. */
    public float nearPlane() {
        return nearPlane;
    }

    /** Far clipping plane distance. */
    public float farPlane() {
        return farPlane;
    }

    // ---- Matrix computation ----

    /**
     * Computes the view matrix based on current orbit parameters.
     */
    public float[] viewMatrix() {
        float ex = (float) (distance * Math.cos(elevation) * Math.sin(azimuth));
        float ey = (float) (distance * Math.sin(elevation));
        float ez = (float) (distance * Math.cos(elevation) * Math.cos(azimuth));
        return MatrixUtils.lookAt(
                targetX + ex, targetY + ey, targetZ + ez,
                targetX, targetY, targetZ,
                0, 1, 0
        );
    }

    /**
     * Computes the projection matrix for the current projection type.
     *
     * @param aspect viewport width / height
     * @return column-major 4x4 projection matrix
     */
    public float[] projectionMatrix(float aspect) {
        if (projectionType == ProjectionType.ORTHOGRAPHIC) {
            // Scale orthographic bounds by distance for zoom-like behavior
            float halfHeight = distance * 0.7f;
            float halfWidth = halfHeight * aspect;
            return MatrixUtils.orthographic(
                    -halfWidth, halfWidth,
                    -halfHeight, halfHeight,
                    nearPlane, farPlane
            );
        }
        return MatrixUtils.perspective(fovDegrees, aspect, nearPlane, farPlane);
    }

    /**
     * Computes the combined view-projection matrix.
     */
    public float[] viewProjectionMatrix(float aspect) {
        return MatrixUtils.multiply(projectionMatrix(aspect), viewMatrix());
    }

    /**
     * Resets the camera to its default position.
     */
    public Camera reset() {
        this.azimuth = (float) Math.toRadians(45);
        this.elevation = (float) Math.toRadians(30);
        this.distance = 12f;
        this.targetX = 0;
        this.targetY = 0;
        this.targetZ = 0;
        this.projectionType = ProjectionType.PERSPECTIVE;
        return this;
    }
}
