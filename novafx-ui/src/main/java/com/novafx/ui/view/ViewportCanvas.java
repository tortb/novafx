package com.novafx.ui.view;

import com.novafx.math.Vector3d;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A 3D viewport that displays sampled point clouds using JavaFX 3D.
 * <p>
 * Supports orbit camera controls (drag to rotate, scroll to zoom).
 * Renders a coordinate grid, axes, and the sampled points as small spheres.
 * <p>
 * <b>Deprecated:</b> Use {@link CanvasViewport} instead for better
 * performance with large point clouds.
 *
 * @deprecated Replaced by {@link CanvasViewport} which uses a 2D Canvas
 *             with software projection for 100k+ points at 60 FPS.
 */
@Deprecated(since = "1.0", forRemoval = false)
public final class ViewportCanvas extends SubScene {

    private static final Logger log = LoggerFactory.getLogger(ViewportCanvas.class);

    private static final double GRID_SIZE = 10.0;
    private static final double GRID_DIVISIONS = 20.0;

    private final Group root;
    private final PerspectiveCamera camera;
    private final Group pointGroup;
    private final Group gridGroup;

    private double mouseX;
    private double mouseY;
    private double cameraDistance = 20.0;
    private double azimuth = 45.0;
    private double elevation = 30.0;

    private double pointRadius = 0.08;
    private Color pointColor = Color.CORNFLOWERBLUE;
    private boolean showGrid = true;

    /**
     * Creates a 3D viewport.
     *
     * @param width  initial width in pixels
     * @param height initial height in pixels
     */
    public ViewportCanvas(double width, double height) {
        super(new Group(), width, height, true, SceneAntialiasing.BALANCED);

        this.root = (Group) getRoot();
        this.camera = new PerspectiveCamera(true);
        this.pointGroup = new Group();
        this.gridGroup = new Group();

        setupCamera();
        buildGrid();
        setupInteraction();

        root.getChildren().addAll(gridGroup, pointGroup);

        setCamera(camera);
        setFill(Color.web("0.05 0.05 0.08"));

        log.info("ViewportCanvas initialized ({}x{})", (int) width, (int) height);
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    /** Updates the displayed point cloud. */
    public void setPoints(List<Vector3d> points) {
        pointGroup.getChildren().clear();
        if (points == null || points.isEmpty()) return;

        PhongMaterial material = new PhongMaterial(pointColor);
        for (Vector3d p : points) {
            Sphere sphere = new Sphere(pointRadius, 4);
            sphere.setMaterial(material);
            sphere.setTranslateX(p.x());
            sphere.setTranslateY(p.y());
            sphere.setTranslateZ(p.z());
            pointGroup.getChildren().add(sphere);
        }
        log.debug("Rendered {} points in viewport", points.size());
    }

    /** Sets the point radius. */
    public void setPointRadius(double radius) {
        this.pointRadius = Math.max(0.01, radius);
        // Re-render with new size
        PhongMaterial material = new PhongMaterial(pointColor);
        for (var node : pointGroup.getChildren()) {
            if (node instanceof Sphere sphere) {
                sphere.setRadius(this.pointRadius);
                sphere.setMaterial(material);
            }
        }
    }

    /** Sets the point color. */
    public void setPointColor(Color color) {
        this.pointColor = color;
        PhongMaterial material = new PhongMaterial(color);
        for (var node : pointGroup.getChildren()) {
            if (node instanceof Sphere sphere) {
                sphere.setMaterial(material);
            }
        }
    }

    /** Shows or hides the grid. */
    public void setShowGrid(boolean show) {
        this.showGrid = show;
        gridGroup.setVisible(show);
    }

    /** Resets the camera to default position. */
    public void resetCamera() {
        cameraDistance = 20.0;
        azimuth = 45.0;
        elevation = 30.0;
        updateCameraPosition();
    }

    // ---------------------------------------------------------------
    // Camera
    // ---------------------------------------------------------------

    private void setupCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        camera.setFieldOfView(60);
        updateCameraPosition();
    }

    private void updateCameraPosition() {
        double radAz = Math.toRadians(azimuth);
        double radEl = Math.toRadians(elevation);
        double x = cameraDistance * Math.cos(radEl) * Math.sin(radAz);
        double y = cameraDistance * Math.sin(radEl);
        double z = cameraDistance * Math.cos(radEl) * Math.cos(radAz);

        camera.getTransforms().clear();
        camera.getTransforms().addAll(
                new Rotate(-elevation, Rotate.X_AXIS),
                new Rotate(-azimuth, Rotate.Y_AXIS),
                new Translate(-x, -y, -z)
        );
    }

    // ---------------------------------------------------------------
    // Grid
    // ---------------------------------------------------------------

    private void buildGrid() {
        gridGroup.getChildren().clear();

        double half = GRID_SIZE;
        double step = (2.0 * GRID_SIZE) / GRID_DIVISIONS;

        // Grid lines in XZ plane
        for (double pos = -half; pos <= half + 0.001; pos += step) {
            // Line parallel to X
            Line lineX = new Line(-half, -half, half, -half);
            lineX.setTranslateX(0);
            lineX.setTranslateZ(pos);
            lineX.setStroke(Color.gray(0.3));
            lineX.setStrokeWidth(0.5);

            // Line parallel to Z
            Line lineZ = new Line(-half, -half, half, -half);
            lineZ.setTranslateX(pos);
            lineZ.setTranslateZ(0);
            lineZ.setStroke(Color.gray(0.3));
            lineZ.setStrokeWidth(0.5);

            gridGroup.getChildren().addAll(lineX, lineZ);
        }
    }

    // ---------------------------------------------------------------
    // Mouse interaction
    // ---------------------------------------------------------------

    private void setupInteraction() {
        setOnMousePressed(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            double dx = event.getSceneX() - mouseX;
            double dy = event.getSceneY() - mouseY;
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();

            if (event.isPrimaryButtonDown()) {
                azimuth += dx * 0.5;
                elevation = Math.max(-89, Math.min(89, elevation - dy * 0.5));
                updateCameraPosition();
            } else if (event.isSecondaryButtonDown()) {
                // Pan
                root.setTranslateX(root.getTranslateX() + dx * 0.02);
                root.setTranslateY(root.getTranslateY() - dy * 0.02);
            }
        });

        setOnScroll(event -> {
            cameraDistance = Math.max(2.0, Math.min(200.0, cameraDistance - event.getDeltaY() * 0.1));
            updateCameraPosition();
        });
    }
}
