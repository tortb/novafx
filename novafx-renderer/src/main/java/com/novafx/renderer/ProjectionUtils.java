package com.novafx.renderer;

/**
 * Utility methods for projecting 3D points to 2D screen coordinates
 * using the same matrix pipeline as the OpenGL renderer.
 * <p>
 * Used by the JavaFX Canvas viewport to render point clouds without
 * requiring a native OpenGL context.
 */
public final class ProjectionUtils {

    private ProjectionUtils() {
    }

    /**
     * Transforms a 3D point by a 4x4 column-major matrix, returning
     * the clip-space (homogeneous) coordinates.
     *
     * @param matrix 16-element column-major 4x4 matrix
     * @param x      x-coordinate
     * @param y      y-coordinate
     * @param z      z-coordinate
     * @return 4-element clip-space coordinates [x, y, z, w]
     */
    public static float[] transform(float[] matrix, float x, float y, float z) {
        float[] result = new float[4];
        result[0] = matrix[0] * x + matrix[4] * y + matrix[8] * z + matrix[12];
        result[1] = matrix[1] * x + matrix[5] * y + matrix[9] * z + matrix[13];
        result[2] = matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14];
        result[3] = matrix[3] * x + matrix[7] * y + matrix[11] * z + matrix[15];
        return result;
    }

    /**
     * Converts clip-space coordinates to 2D screen coordinates.
     * <p>
     * Performs perspective divide (x/w, y/w) and maps from NDC [-1, 1]
     * to pixel coordinates.
     *
     * @param clip   4-element clip-space coordinates from {@link #transform}
     * @param width  viewport width in pixels
     * @param height viewport height in pixels
     * @return 2-element array [screenX, screenY]; or null if the point is
     *         behind the camera (w <= 0)
     */
    public static float[] toScreen(float[] clip, int width, int height) {
        if (clip[3] <= 0) return null; // behind camera

        float invW = 1.0f / clip[3];
        float ndcX = clip[0] * invW;
        float ndcY = clip[1] * invW;

        // Check if point is outside view frustum
        if (Math.abs(ndcX) > 1.0f || Math.abs(ndcY) > 1.0f) return null;

        float screenX = (ndcX + 1.0f) * 0.5f * width;
        float screenY = (1.0f - ndcY) * 0.5f * height; // flip Y

        return new float[]{screenX, screenY};
    }

    /**
     * Convenience method: transforms a 3D point using the given
     * view-projection matrix and returns screen coordinates.
     *
     * @param vpMatrix 16-element column-major VP matrix
     * @param x        x-coordinate
     * @param y        y-coordinate
     * @param z        z-coordinate
     * @param width    viewport width
     * @param height   viewport height
     * @return [screenX, screenY] or null if off-screen
     */
    public static float[] project(float[] vpMatrix, float x, float y, float z,
                                  int width, int height) {
        float[] clip = transform(vpMatrix, x, y, z);
        return toScreen(clip, width, height);
    }
}
