package com.novafx.renderer;

import com.novafx.math.Vector3d;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;

/**
 * GPU-accelerated point cloud renderer.
 * <p>
 * Uploads point data as a direct {@link FloatBuffer} to a VBO and
 * renders with a single {@code glDrawArrays(GL_POINTS)} call.
 * <p>
 * Performance targets:
 * <ul>
 *   <li>100k points: 60+ FPS</li>
 *   <li>10M points: 30+ FPS (GPU-bound)</li>
 * </ul>
 */
public final class PointRenderer {

    private static final Logger log = LoggerFactory.getLogger(PointRenderer.class);

    private static final String VERTEX_SHADER = """
            #version 330 core
            layout(location = 0) in vec3 aPos;
            uniform mat4 uMVP;
            uniform float uPointSize;
            void main() {
                gl_Position = uMVP * vec4(aPos, 1.0);
                gl_PointSize = uPointSize / (1.0 + length(gl_Position.xyz / gl_Position.w));
            }
            """;

    private static final String FRAGMENT_SHADER = """
            #version 330 core
            uniform vec3 uColor;
            out vec4 fragColor;
            void main() {
                fragColor = vec4(uColor, 1.0);
            }
            """;

    private ShaderProgram shader;
    private int vao;
    private int vbo;
    private int vertexCount;
    private float pointSize;
    private final float[] color;
    private boolean initialized;

    private int mvpUniform;
    private int colorUniform;
    private int pointSizeUniform;

    // Reusable direct buffer to avoid reallocation
    private FloatBuffer reusableBuffer;

    /** Creates a point renderer with default size 3 and white color. */
    public PointRenderer() {
        this.pointSize = 3f;
        this.color = new float[]{1f, 1f, 1f};
    }

    /**
     * Initializes the shader program and VAO/VBO.
     *
     * @param points initial point cloud
     */
    public void init(List<Vector3d> points) {
        this.shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        this.mvpUniform = shader.getUniformLocation("uMVP");
        this.colorUniform = shader.getUniformLocation("uColor");
        this.pointSizeUniform = shader.getUniformLocation("uPointSize");

        createVAO();
        uploadPoints(points);
        initialized = true;
        log.info("PointRenderer initialized");
    }

    /**
     * Updates point data from a {@link List} of {@link Vector3d}.
     *
     * @param points new point data
     */
    public void updatePoints(List<Vector3d> points) {
        if (!initialized) return;
        uploadPoints(points);
    }

    /**
     * Uploads point data directly from a {@link FloatBuffer}.
     * <p>
     * The buffer should contain interleaved x, y, z values
     * (3 floats per point). After upload the buffer position is
     * undefined.
     *
     * @param buffer FloatBuffer containing vertex data
     * @param count  number of points
     */
    public void uploadPoints(FloatBuffer buffer, int count) {
        if (!initialized) return;
        if (buffer == null || count <= 0) {
            vertexCount = 0;
            return;
        }

        buffer.rewind();
        vertexCount = count;

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    /**
     * Renders the point cloud as GL_POINTS.
     *
     * @param vpMatrix column-major 4x4 view-projection matrix
     */
    public void renderPoints(float[] vpMatrix) {
        if (!initialized || vertexCount == 0) return;

        shader.use();
        shader.setUniformMatrix4(mvpUniform, vpMatrix);
        shader.setUniformVec3(colorUniform, color[0], color[1], color[2]);
        shader.setUniformFloat(pointSizeUniform, pointSize);

        glBindVertexArray(vao);
        glDrawArrays(GL_POINTS, 0, vertexCount);
        glBindVertexArray(0);

        ShaderProgram.unbind();
    }

    /**
     * Renders the point cloud as a line strip.
     *
     * @param vpMatrix column-major 4x4 view-projection matrix
     */
    public void renderLines(float[] vpMatrix) {
        if (!initialized || vertexCount == 0) return;

        shader.use();
        shader.setUniformMatrix4(mvpUniform, vpMatrix);
        shader.setUniformVec3(colorUniform, color[0], color[1], color[2]);
        shader.setUniformFloat(pointSizeUniform, pointSize);

        glBindVertexArray(vao);
        glDrawArrays(GL_LINE_STRIP, 0, vertexCount);
        glBindVertexArray(0);

        ShaderProgram.unbind();
    }

    /** Returns the current point size. */
    public float pointSize() {
        return pointSize;
    }

    /** Sets the base point size (automatically attenuated by distance). */
    public void setPointSize(float size) {
        this.pointSize = Math.max(1f, size);
    }

    /** Sets the RGB color (components in [0, 1]). */
    public void setColor(float r, float g, float b) {
        this.color[0] = r;
        this.color[1] = g;
        this.color[2] = b;
    }

    /** Returns the number of vertices currently uploaded. */
    public int vertexCount() {
        return vertexCount;
    }

    /** Releases GPU resources. */
    public void cleanup() {
        if (initialized) {
            glDeleteVertexArrays(vao);
            glDeleteBuffers(vbo);
            shader.cleanup();
            initialized = false;
            log.info("PointRenderer cleaned up");
        }
    }

    // ---------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------

    private void createVAO() {
        int[] vaoArr = new int[1];
        int[] vboArr = new int[1];
        glGenVertexArrays(vaoArr);
        glGenBuffers(vboArr);
        vao = vaoArr[0];
        vbo = vboArr[0];
    }

    private void uploadPoints(List<Vector3d> points) {
        if (points == null || points.isEmpty()) {
            vertexCount = 0;
            return;
        }

        int count = points.size();
        int floatsNeeded = count * 3;

        // Reuse or allocate direct FloatBuffer
        if (reusableBuffer == null || reusableBuffer.capacity() < floatsNeeded) {
            reusableBuffer = BufferUtils.createFloatBuffer(floatsNeeded);
        }

        reusableBuffer.rewind();
        for (Vector3d p : points) {
            reusableBuffer.put((float) p.x());
            reusableBuffer.put((float) p.y());
            reusableBuffer.put((float) p.z());
        }
        reusableBuffer.flip();

        uploadPoints(reusableBuffer, count);
    }
}
