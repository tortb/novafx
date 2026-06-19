package com.novafx.renderer;

import com.novafx.math.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;

/**
 * Renders a coordinate grid with X (red), Y (green), Z (blue) axes
 * and a ground-plane grid in the XZ-plane.
 * <p>
 * The grid spans {@code [-gridSize, gridSize]} with {@code subdivisions}
 * cells per side.
 */
public final class GridRenderer {

    private static final Logger log = LoggerFactory.getLogger(GridRenderer.class);

    private final int gridSize;
    private final int subdivisions;

    private int gridVao;
    private int gridVbo;
    private int gridVertexCount;

    private int axisVao;
    private int axisVbo;
    private int axisVertexCount;

    private boolean initialized;

    /** Creates a grid renderer with the given extent and density. */
    public GridRenderer(int gridSize, int subdivisions) {
        this.gridSize = gridSize;
        this.subdivisions = subdivisions;
    }

    /** Creates a default grid: size 10, 20 subdivisions (0.5 unit spacing). */
    public GridRenderer() {
        this(10, 20);
    }

    /**
     * Initializes OpenGL buffers for the grid and axes.
     * Must be called once after an OpenGL context is current.
     */
    public void init() {
        buildGrid();
        buildAxes();
        initialized = true;
        log.info("GridRenderer initialized ({} grid vertices, {} axis vertices)",
                gridVertexCount, axisVertexCount);
    }

    /**
     * Renders the grid and axes using the given view-projection matrix.
     *
     * @param vpMatrix column-major 4x4 view-projection matrix
     */
    public void render(float[] vpMatrix) {
        if (!initialized) return;

        glEnable(GL_DEPTH_TEST);

        // --- Grid ---
        glBindVertexArray(gridVao);
        glDrawArrays(GL_LINES, 0, gridVertexCount);
        glBindVertexArray(0);

        // --- Axes ---
        glBindVertexArray(axisVao);
        glDrawArrays(GL_LINES, 0, axisVertexCount);
        glBindVertexArray(0);
    }

    /**
     * Releases GPU resources.
     */
    public void cleanup() {
        if (initialized) {
            glDeleteVertexArrays(gridVao);
            glDeleteBuffers(gridVbo);
            glDeleteVertexArrays(axisVao);
            glDeleteBuffers(axisVbo);
            initialized = false;
            log.info("GridRenderer cleaned up");
        }
    }

    // ---------------------------------------------------------------
    // Grid construction
    // ---------------------------------------------------------------

    private void buildGrid() {
        float half = gridSize;
        float step = 2f * gridSize / subdivisions;
        List<Float> vertices = new ArrayList<>();

        for (int i = 0; i <= subdivisions; i++) {
            float pos = -half + i * step;
            // Line parallel to X
            vertices.add(pos);
            vertices.add(0f);
            vertices.add(-half);
            vertices.add(pos);
            vertices.add(0f);
            vertices.add(half);
            // Line parallel to Z
            vertices.add(-half);
            vertices.add(0f);
            vertices.add(pos);
            vertices.add(half);
            vertices.add(0f);
            vertices.add(pos);
        }

        float[] data = toFloatArray(vertices);
        gridVertexCount = vertices.size() / 6;

        int[] vao = new int[1];
        int[] vbo = new int[1];
        glGenVertexArrays(vao);
        glGenBuffers(vbo);

        gridVao = vao[0];
        gridVbo = vbo[0];

        glBindVertexArray(gridVao);
        glBindBuffer(GL_ARRAY_BUFFER, gridVbo);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
    }

    private void buildAxes() {
        // Axis lines: X (red), Y (green), Z (blue), each from origin to axisLength
        float axisLength = gridSize + 1f;
        List<Float> verts = new ArrayList<>();

        // X axis: origin to (axisLength, 0, 0)
        verts.add(0f); verts.add(0f); verts.add(0f);
        verts.add(1f); verts.add(0f); verts.add(0f); // color
        verts.add(axisLength); verts.add(0f); verts.add(0f);
        verts.add(1f); verts.add(0f); verts.add(0f);

        // Y axis
        verts.add(0f); verts.add(0f); verts.add(0f);
        verts.add(0f); verts.add(1f); verts.add(0f);
        verts.add(0f); verts.add(axisLength); verts.add(0f);
        verts.add(0f); verts.add(1f); verts.add(0f);

        // Z axis
        verts.add(0f); verts.add(0f); verts.add(0f);
        verts.add(0f); verts.add(0f); verts.add(1f);
        verts.add(0f); verts.add(0f); verts.add(axisLength);
        verts.add(0f); verts.add(0f); verts.add(1f);

        float[] data = toFloatArray(verts);
        axisVertexCount = 6; // 6 vertices (3 lines × 2)

        int[] vao = new int[1];
        int[] vbo = new int[1];
        glGenVertexArrays(vao);
        glGenBuffers(vbo);

        axisVao = vao[0];
        axisVbo = vbo[0];

        glBindVertexArray(axisVao);
        glBindBuffer(GL_ARRAY_BUFFER, axisVbo);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
        // position attribute (location 0): 3 floats
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        // color attribute (location 1): 3 floats
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }
}
