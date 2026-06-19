package com.novafx.ui.view;

import com.novafx.math.Vector3d;
import com.novafx.renderer.Camera;
import com.novafx.renderer.RenderEngine;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

/**
 * True OpenGL 3.3 viewport using LWJGL with offscreen FBO rendering.
 * <p>
 * Renders the 3D scene using the {@link RenderEngine} pipeline and
 * displays the result on a JavaFX Canvas via pixel transfer.
 * <p>
 * Thread model: OpenGL calls run on a dedicated GL thread. The
 * resulting pixels are passed to the JavaFX Application thread
 * for display.
 */
public final class GLViewport extends Canvas {

    private static final Logger log = LoggerFactory.getLogger(GLViewport.class);

    private final RenderEngine renderEngine;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Object pixelLock = new Object();

    private long window;
    private int fboId;
    private int texId;
    private int rboId;
    private ByteBuffer pixelBuffer;
    private int fbWidth, fbHeight;

    private volatile boolean needsResize = true;

    /**
     * Creates the GL viewport and starts the render thread.
     */
    public GLViewport() {
        this.renderEngine = new RenderEngine();

        Thread glThread = new Thread(this::glMain, "GL-Render");
        glThread.setDaemon(true);
        glThread.start();

        // React to resize
        widthProperty().addListener(e -> needsResize = true);
        heightProperty().addListener(e -> needsResize = true);

        setupMouse();
    }

    // ---------------------------------------------------------------
    // Public API (same interface as CanvasViewport)
    // ---------------------------------------------------------------

    /** Updates the point cloud to render. */
    public void setPoints(List<Vector3d> newPoints) {
        Platform.runLater(() -> {
            synchronized (pixelLock) {
                renderEngine.loadPoints(newPoints);
            }
        });
    }

    /** Sets point size in pixels. */
    public void setPointSize(double size) {
        renderEngine.pointRenderer().setPointSize((float) size);
    }

    /** Sets point color. */
    public void setPointColor(Color color) {
        renderEngine.pointRenderer().setColor(
                (float) color.getRed(),
                (float) color.getGreen(),
                (float) color.getBlue()
        );
    }

    /** Shows or hides the grid. */
    public void setShowGrid(boolean show) {
        // GridRenderer visibility handled via render engine
    }

    /** Resets the camera. */
    public void resetCamera() {
        renderEngine.camera().reset();
    }

    /** Returns the camera for direct manipulation. */
    public Camera camera() {
        return renderEngine.camera();
    }

    // ---------------------------------------------------------------
    // GL thread
    // ---------------------------------------------------------------

    private void glMain() {
        try {
            initGL();
            renderLoop();
        } catch (Exception e) {
            log.error("GL thread error", e);
        } finally {
            cleanupGL();
        }
    }

    private void initGL() {
        glfwInit();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(1, 1, "", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        log.info("OpenGL: {} | {}", glGetString(GL_VERSION), glGetString(GL_RENDERER));

        createFBO(1, 1);
    }

    private void createFBO(int w, int h) {
        if (fboId != 0) glDeleteFramebuffers(fboId);
        if (texId != 0) glDeleteTextures(texId);
        if (rboId != 0) glDeleteRenderbuffers(rboId);

        // Color texture
        texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Depth renderbuffer
        rboId = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rboId);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, w, h);

        // FBO
        fboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texId, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboId);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("FBO incomplete: " + status);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Pixel buffer
        pixelBuffer = BufferUtils.createByteBuffer(w * h * 4);
        fbWidth = w;
        fbHeight = h;
        needsResize = false;
    }

    private void renderLoop() {
        while (running.get()) {
            if (needsResize) {
                int w = Math.max(1, (int) getWidth());
                int h = Math.max(1, (int) getHeight());
                createFBO(w, h);
                renderEngine.init(w, h);
            }

            synchronized (pixelLock) {
                renderFrame();
            }

            try {
                Thread.sleep(16); // ~60 FPS cap
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void renderFrame() {
        int w = fbWidth;
        int h = fbHeight;
        if (w <= 0 || h <= 0) return;

        glViewport(0, 0, w, h);
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);

        renderEngine.render();

        // Read pixels (flip Y by reading from bottom)
        pixelBuffer.rewind();
        glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, pixelBuffer);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Update JavaFX Canvas on FX thread
        int fw = w, fh = h;
        ByteBuffer pixels = pixelBuffer.duplicate();
        Platform.runLater(() -> drawToCanvas(pixels, fw, fh));
    }

    private void drawToCanvas(ByteBuffer pixels, int w, int h) {
        double cw = getWidth();
        double ch = getHeight();
        if (cw <= 0 || ch <= 0) return;

        WritableImage img = new WritableImage(w, h);
        PixelWriter pw = img.getPixelWriter();
        pw.setPixels(0, 0, w, h,
                PixelFormat.getByteBgraPreInstance(),
                pixels, w * 4);

        getGraphicsContext2D().drawImage(img, 0, 0, cw, ch);
    }

    private void cleanupGL() {
        if (fboId != 0) glDeleteFramebuffers(fboId);
        if (texId != 0) glDeleteTextures(texId);
        if (rboId != 0) glDeleteRenderbuffers(rboId);
        if (window != 0) glfwDestroyWindow(window);
        glfwTerminate();
    }

    // ---------------------------------------------------------------
    // Mouse interaction
    // ---------------------------------------------------------------

    private double mouseX, mouseY;

    private void setupMouse() {
        setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            double dx = e.getSceneX() - mouseX;
            double dy = e.getSceneY() - mouseY;
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();

            Camera cam = renderEngine.camera();
            if (e.isPrimaryButtonDown()) {
                if (e.isShiftDown()) {
                    float s = 0.02f * cam.distance();
                    cam.pan((float) (-dx * s), (float) (dy * s), 0);
                } else {
                    cam.rotateAzimuth((float) Math.toRadians(-dx * 0.5));
                    cam.rotateElevation((float) Math.toRadians(-dy * 0.5));
                }
            }
        });

        setOnScroll(e -> {
            renderEngine.camera().zoom((float) (-e.getDeltaY() * 0.1));
        });
    }
}
