package com.novafx.renderer;

import com.novafx.math.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.lwjgl.opengl.GL33.*;

/**
 * Coordinates the full rendering pipeline: camera, grid, and point cloud.
 * <p>
 * Manages the OpenGL state machine and delegates draw calls to
 * {@link GridRenderer} and {@link PointRenderer}.
 */
public final class RenderEngine {

    private static final Logger log = LoggerFactory.getLogger(RenderEngine.class);

    private final Camera camera;
    private final GridRenderer gridRenderer;
    private final PointRenderer pointRenderer;

    private int viewportWidth;
    private int viewportHeight;
    private boolean initialized;

    private static final float[] BG_COLOR = {0.05f, 0.05f, 0.08f};

    /** Creates a render engine with default components. */
    public RenderEngine() {
        this.camera = new Camera();
        this.gridRenderer = new GridRenderer();
        this.pointRenderer = new PointRenderer();
    }

    /**
     * Initializes the render engine. Must be called once after an
     * OpenGL context is made current.
     *
     * @param width  initial viewport width in pixels
     * @param height initial viewport height in pixels
     */
    public void init(int width, int height) {
        log.info("Initializing RenderEngine ({}x{})", width, height);

        viewportWidth = width;
        viewportHeight = height;

        glViewport(0, 0, width, height);
        glClearColor(BG_COLOR[0], BG_COLOR[1], BG_COLOR[2], 1f);
        glEnable(GL_DEPTH_TEST);

        gridRenderer.init();
        initialized = true;
    }

    /**
     * Loads point data into the GPU. Must be called after {@link #init}.
     *
     * @param points the point cloud
     */
    public void loadPoints(List<Vector3d> points) {
        if (!initialized) return;
        if (pointRenderer.vertexCount() == 0) {
            pointRenderer.init(points);
        } else {
            pointRenderer.updatePoints(points);
        }
    }

    /**
     * Renders one frame. Must be called each frame from the rendering loop.
     */
    public void render() {
        if (!initialized) return;

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        float aspect = (float) viewportWidth / (float) Math.max(viewportHeight, 1);
        float[] vpMatrix = camera.viewProjectionMatrix(aspect);

        gridRenderer.render(vpMatrix);
        pointRenderer.renderPoints(vpMatrix);
    }

    /** Handle viewport resize. */
    public void resize(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        glViewport(0, 0, width, height);
    }

    /** Returns the camera for external manipulation. */
    public Camera camera() {
        return camera;
    }

    /** Returns the point renderer for configuration. */
    public PointRenderer pointRenderer() {
        return pointRenderer;
    }

    /**
     * Releases all GPU resources.
     */
    public void cleanup() {
        if (initialized) {
            gridRenderer.cleanup();
            pointRenderer.cleanup();
            initialized = false;
            log.info("RenderEngine cleaned up");
        }
    }
}
