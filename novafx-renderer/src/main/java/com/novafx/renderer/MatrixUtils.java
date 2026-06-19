package com.novafx.renderer;

/**
 * Utility methods for constructing 4x4 transformation matrices used in OpenGL.
 * <p>
 * Matrices are stored in column-major order as flat {@code float[16]} arrays,
 * as required by {@code glUniformMatrix4fv}.
 */
public final class MatrixUtils {

    private MatrixUtils() {
        // utility class
    }

    /** Returns a 4x4 identity matrix in column-major order. */
    public static float[] identity() {
        return new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        };
    }

    /**
     * Multiplies two 4x4 matrices: {@code result = a * b}.
     * Both inputs and the output are 16-element column-major arrays.
     */
    public static float[] multiply(float[] a, float[] b) {
        float[] result = new float[16];
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += a[k * 4 + row] * b[col * 4 + k];
                }
                result[col * 4 + row] = sum;
            }
        }
        return result;
    }

    /**
     * Builds a perspective projection matrix.
     *
     * @param fovDegrees     vertical field of view in degrees
     * @param aspect         width / height ratio
     * @param near           near clipping plane distance
     * @param far            far clipping plane distance
     * @return column-major perspective projection matrix
     */
    public static float[] perspective(float fovDegrees, float aspect, float near, float far) {
        float fovRad = (float) Math.toRadians(fovDegrees);
        float f = (float) (1.0 / Math.tan(fovRad / 2.0));
        float[] m = new float[16];
        m[0] = f / aspect;
        m[5] = f;
        m[10] = (far + near) / (near - far);
        m[11] = -1;
        m[14] = (2 * far * near) / (near - far);
        return m;
    }

    /**
     * Builds an orthographic projection matrix.
     * <p>
     * Maps world coordinates to normalized device coordinates [-1, 1]
     * without perspective division.  Useful for 2D views and isometric
     * projections.
     *
     * @param left   left clipping plane
     * @param right  right clipping plane
     * @param bottom bottom clipping plane
     * @param top    top clipping plane
     * @param near   near clipping plane
     * @param far    far clipping plane
     * @return column-major orthographic projection matrix
     */
    public static float[] orthographic(float left, float right,
                                        float bottom, float top,
                                        float near, float far) {
        float[] m = identity();
        m[0] = 2f / (right - left);
        m[5] = 2f / (top - bottom);
        m[10] = -2f / (far - near);
        m[12] = -(right + left) / (right - left);
        m[13] = -(top + bottom) / (top - bottom);
        m[14] = -(far + near) / (far - near);
        return m;
    }

    /**
     * Builds a look-at view matrix.
     *
     * @param eyeX   camera position x
     * @param eyeY   camera position y
     * @param eyeZ   camera position z
     * @param centerX target position x (point the camera looks at)
     * @param centerY target position y
     * @param centerZ target position z
     * @param upX     up vector x
     * @param upY     up vector y
     * @param upZ     up vector z
     * @return column-major view matrix
     */
    public static float[] lookAt(float eyeX, float eyeY, float eyeZ,
                                 float centerX, float centerY, float centerZ,
                                 float upX, float upY, float upZ) {
        float[] f = normalize(centerX - eyeX, centerY - eyeY, centerZ - eyeZ);
        float[] c = cross(f[0], f[1], f[2], upX, upY, upZ);
        float[] s = normalize(c[0], c[1], c[2]);
        float[] u = cross(s[0], s[1], s[2], f[0], f[1], f[2]);

        float[] m = identity();
        m[0] = s[0];
        m[1] = u[0];
        m[2] = -f[0];
        m[4] = s[1];
        m[5] = u[1];
        m[6] = -f[1];
        m[8] = s[2];
        m[9] = u[2];
        m[10] = -f[2];
        m[12] = -dot(s, eyeX, eyeY, eyeZ);
        m[13] = -dot(u, eyeX, eyeY, eyeZ);
        m[14] = dot(f, eyeX, eyeY, eyeZ);
        return m;
    }

    private static float[] normalize(float x, float y, float z) {
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len == 0) return new float[]{0, 0, 0};
        return new float[]{x / len, y / len, z / len};
    }

    private static float[] cross(float ax, float ay, float az, float bx, float by, float bz) {
        return new float[]{
                ay * bz - az * by,
                az * bx - ax * bz,
                ax * by - ay * bx
        };
    }

    private static float dot(float[] v, float x, float y, float z) {
        return v[0] * x + v[1] * y + v[2] * z;
    }
}
