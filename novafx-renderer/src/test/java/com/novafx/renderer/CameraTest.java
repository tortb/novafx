package com.novafx.renderer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CameraTest {

    @Test
    void shouldCreateWithDefaultValues() {
        Camera cam = new Camera();
        assertThat(cam.distance()).isEqualTo(12f);
        assertThat(cam.azimuth()).isCloseTo((float) Math.toRadians(45), within(0.001f));
        assertThat(cam.elevation()).isCloseTo((float) Math.toRadians(30), within(0.001f));
        assertThat(cam.fovDegrees()).isEqualTo(60);
    }

    @Test
    void shouldRotateAzimuth() {
        Camera cam = new Camera();
        float before = cam.azimuth();
        cam.rotateAzimuth(0.5f);
        assertThat(cam.azimuth()).isCloseTo(before + 0.5f, within(0.001f));
    }

    @Test
    void shouldRotateAzimuthNegative() {
        Camera cam = new Camera();
        cam.rotateAzimuth(-1.0f);
        assertThat(cam.azimuth()).isCloseTo((float) Math.toRadians(45) - 1.0f, within(0.001f));
    }

    @Test
    void shouldClampElevationTo89Degrees() {
        Camera cam = new Camera();
        cam.rotateElevation(5.0f); // way beyond 89 degrees
        assertThat(cam.elevation()).isLessThan((float) Math.toRadians(90));
        assertThat(cam.elevation()).isCloseTo((float) Math.toRadians(89), within(0.001f));
    }

    @Test
    void shouldClampElevationNegative() {
        Camera cam = new Camera();
        cam.rotateElevation(-5.0f);
        assertThat(cam.elevation()).isCloseTo((float) Math.toRadians(-89), within(0.001f));
    }

    @Test
    void shouldZoom() {
        Camera cam = new Camera();
        cam.zoom(5f);
        assertThat(cam.distance()).isEqualTo(17f);
    }

    @Test
    void shouldZoomIn() {
        Camera cam = new Camera();
        cam.zoom(-5f);
        assertThat(cam.distance()).isEqualTo(7f);
    }

    @Test
    void shouldNotZoomBelowMinimum() {
        Camera cam = new Camera();
        cam.zoom(-100f);
        assertThat(cam.distance()).isEqualTo(0.5f);
    }

    @Test
    void shouldPan() {
        Camera cam = new Camera();
        cam.pan(10f, 20f, 30f);
        assertThat(cam.targetX()).isEqualTo(10f);
        assertThat(cam.targetY()).isEqualTo(20f);
        assertThat(cam.targetZ()).isEqualTo(30f);
    }

    @Test
    void shouldReset() {
        Camera cam = new Camera();
        cam.rotateAzimuth(1f);
        cam.zoom(20f);
        cam.pan(5, 5, 5);
        cam.reset();
        assertThat(cam.azimuth()).isCloseTo((float) Math.toRadians(45), within(0.001f));
        assertThat(cam.distance()).isEqualTo(12f);
        assertThat(cam.targetX()).isEqualTo(0);
    }

    @Test
    void shouldProduceViewMatrix() {
        Camera cam = new Camera();
        float[] view = cam.viewMatrix();
        assertThat(view).hasSize(16);
    }

    @Test
    void shouldProduceProjectionMatrix() {
        Camera cam = new Camera();
        float[] proj = cam.projectionMatrix(16f / 9f);
        assertThat(proj).hasSize(16);
    }

    @Test
    void shouldProduceViewProjectionMatrix() {
        Camera cam = new Camera();
        float[] vp = cam.viewProjectionMatrix(16f / 9f);
        assertThat(vp).hasSize(16);
    }

    @Test
    void elevationShouldBeClampedAtConstruction() {
        Camera cam = new Camera();
        // Should be within range initially
        assertThat(cam.elevation()).isBetween((float) -Math.toRadians(89), (float) Math.toRadians(89));
    }

    @Test
    void shouldSupportChainedOperations() {
        Camera cam = new Camera();
        cam.rotateAzimuth(0.1f).rotateElevation(0.05f).zoom(-2f).pan(1, 0, 0);
        assertThat(cam.azimuth()).isCloseTo((float) Math.toRadians(45) + 0.1f, within(0.001f));
        assertThat(cam.elevation()).isCloseTo((float) Math.toRadians(30) + 0.05f, within(0.001f));
        assertThat(cam.distance()).isEqualTo(10f);
        assertThat(cam.targetX()).isEqualTo(1f);
    }
}
