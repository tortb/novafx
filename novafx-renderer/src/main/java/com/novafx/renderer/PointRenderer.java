package com.novafx.renderer;

import com.novafx.math.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.lwjgl.opengl.GL33.*;

/**
 * Renders a point cloud from a list of {@link Vector3d} points.
 * <p>
 * Supports two modes:
 * <ul>
 *   <li><b>Points</b> — each vertex rendered as a GL_POINT</li>
 *   <li><b>Wireframe / Lines</b> — consecutive vertices connected by GL_LINE_STRIP</li>
 * </ul>
 * Uses a shared shader program for point/line rendering with configurable
 * point size and color.
 */
public final class PointRenderer {

    private static final Logger log = LoggerFactory.getLogger(PointRenderer.class);

    private static final String VERTEX_SHADER = """
            #version 330 core
            layout(location = 0) in vec3 aPos;
            uniform mat4 uMVP;
            void main() {
                gl_Position = uMVP * vec4(aPos, 1.0);
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

    /** Creates a point renderer with default size 3 and white color. */
    public PointRenderer() {
        this.pointSize = 3f;
        this.color = new float[]{1f, 1f, 1f};
    }

    /**
     * Initializes the shader program and Vertex Array Object.
     * Must be called once after an OpenGL context is current.
     *
     * @param points the point cloud data
     */
    public void init(List<Vector3d> points) {
        this.shader = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        this.mvpUniform = shader.getUniformLocation("uMVP");
        this.colorUniform = shader.getUniformLocation("uColor");

        uploadPoints(points);
        initialized = true;
        log.info("PointRenderer initialized with {} points", vertexCount);
    }

    /**
     * Updates the point cloud data in the GPU buffer.
     *
     * @param points new point data
     */
    public void updatePoints(List<Vector3d> points) {
        if (!initialized) return;
        uploadPoints(points);
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

        glBindVertexArray(vao);
        glPointSize(pointSize);
        glDrawArrays(GL_POINTS, 0, vertexCount);
        glBindVertexArray(0);

        ShaderProgram.unbind();
    }

    /**
     * Renders the point cloud as a continuous line strip.
     *
     * @param vpMatrix column-major 4x4 view-projection matrix
     */
    public void renderLines(float[] vpMatrix) {
        if (!initialized || vertexCount == 0) return;

        shader.use();
        shader.setUniformMatrix4(mvpUniform, vpMatrix);
        shader.setUniformVec3(colorUniform, color[0], color[1], color[2]);

        glBindVertexArray(vao);
        glDrawArrays(GL_LINE_STRIP, 0, vertexCount);
        glBindVertexArray(0);

        ShaderProgram.unbind();
    }

    /** Current point size in pixels. */
    public float pointSize() {
        return pointSize;
    }

    /** Sets the point size in pixels. */
    public void setPointSize(float size) {
        this.pointSize = Math.max(1f, size);
    }

    /** Sets the RGB color for rendering (values in [0, 1]). */
    public void setColor(float r, float g, float b) {
        this.color[0] = r;
        this.color[1] = g;
        this.color[2] = b;
    }

    /** Number of vertices currently uploaded. */
    public int vertexCount() {
        return vertexCount;
    }

    /**
     * Releases GPU resources.
     */
    public void cleanup() {
        if (initialized) {
            glDeleteVertexArrays(vao);
            glDeleteBuffers(vbo);
            shader.cleanup();
            initialized = false;
            log.info("PointRenderer cleaned up");
        }
    }

    private void uploadPoints(List<Vector3d> points) {
        if (points == null || points.isEmpty()) {
            vertexCount = 0;
            return;
        }

        float[] data = new float[points.size() * 3];
        int idx = 0;
        for (Vector3d p : points) {
            data[idx++] = (float) p.x();
            data[idx++] = (float) p.y();
            data[idx++] = (float) p.z();
        }
        vertexCount = points.size();

        if (vao == 0) {
            int[] vaoArr = new int[1];
            int[] vboArr = new int[1];
            glGenVertexArrays(vaoArr);
            glGenBuffers(vboArr);
            vao = vaoArr[0];
            vbo = vboArr[0];
        }

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, data, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
    }
}
