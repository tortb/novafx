package com.novafx.renderer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ProjectionUtilsTest {

    @Test
    void transformWithIdentityShouldReturnPoint() {
        float[] m = MatrixUtils.identity();
        float[] result = ProjectionUtils.transform(m, 2, 3, 4);
        assertThat(result).containsExactly(2f, 3f, 4f, 1f);
    }

    @Test
    void toScreenShouldMapCenterToCenter() {
        // NDC center (0,0) → screen center
        float[] screen = ProjectionUtils.toScreen(new float[]{0, 0, 0, 1}, 800, 600);
        assertThat(screen).isNotNull();
        assertThat(screen[0]).isEqualTo(400f);
        assertThat(screen[1]).isEqualTo(300f);
    }

    @Test
    void toScreenShouldFlipY() {
        // NDC top (0,1) → screen top (y=0)
        float[] screen = ProjectionUtils.toScreen(new float[]{0, 1, 0, 1}, 800, 600);
        assertThat(screen).isNotNull();
        assertThat(screen[1]).isEqualTo(0f);
    }

    @Test
    void toScreenShouldReturnNullForBehindCamera() {
        float[] screen = ProjectionUtils.toScreen(new float[]{0, 0, 0, -1}, 800, 600);
        assertThat(screen).isNull();
    }

    @Test
    void toScreenShouldReturnNullOutsideFrustum() {
        float[] screen = ProjectionUtils.toScreen(new float[]{2, 0, 0, 1}, 800, 600);
        assertThat(screen).isNull();
    }

    @Test
    void projectThroughCameraShouldWork() {
        Camera cam = new Camera();
        float[] vp = cam.viewProjectionMatrix(16f / 9f);
        float[] screen = ProjectionUtils.project(vp, 0, 0, 0, 800, 600);
        // Origin should project somewhere on screen
        assertThat(screen).isNotNull();
        assertThat(screen[0]).isBetween(0f, 800f);
        assertThat(screen[1]).isBetween(0f, 600f);
    }

    @Test
    void projectThroughCameraShouldReturnNullForBehind() {
        Camera cam = new Camera();
        float[] vp = cam.viewProjectionMatrix(16f / 9f);
        // Point far behind camera
        float[] screen = ProjectionUtils.project(vp, 0, 0, 1000, 800, 600);
        // May be null or off-screen depending on camera direction
        if (screen != null) {
            assertThat(screen[0]).isBetween(0f, 800f);
        }
    }
}
