package com.novafx.renderer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MatrixUtilsTest {

    @Test
    void identityShouldHaveOnesOnDiagonal() {
        float[] m = MatrixUtils.identity();
        assertThat(m[0]).isEqualTo(1);
        assertThat(m[5]).isEqualTo(1);
        assertThat(m[10]).isEqualTo(1);
        assertThat(m[15]).isEqualTo(1);
    }

    @Test
    void identityShouldHaveZerosOffDiagonal() {
        float[] m = MatrixUtils.identity();
        assertThat(m[1]).isEqualTo(0);
        assertThat(m[4]).isEqualTo(0);
        assertThat(m[3]).isEqualTo(0);
    }

    @Test
    void multiplyByIdentityShouldReturnIdentity() {
        float[] a = MatrixUtils.identity();
        float[] b = MatrixUtils.identity();
        float[] result = MatrixUtils.multiply(a, b);
        assertThat(result).containsExactly(MatrixUtils.identity());
    }

    @Test
    void multiplyYieldsCorrectResult() {
        // Simple translation * identity = translation
        float[] trans = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                5, 6, 7, 1
        };
        float[] result = MatrixUtils.multiply(MatrixUtils.identity(), trans);
        assertThat(result).containsExactly(trans);
    }

    @Test
    void perspectiveShouldSetClipping() {
        float[] p = MatrixUtils.perspective(60, 16f / 9f, 0.1f, 100f);
        assertThat(p[15]).isEqualTo(0); // m[3][3] in col-major is index 15? Let me check
        // Actually in column-major: m[3][3] = index 15
        assertThat(p[15]).isZero();
        assertThat(p[11]).isEqualTo(-1); // m[2][3] = -1 for perspective
    }

    @Test
    void lookAtShouldProduceValidMatrix() {
        float[] view = MatrixUtils.lookAt(5, 5, 5, 0, 0, 0, 0, 1, 0);
        // 4x4 matrix should have 16 elements
        assertThat(view).hasSize(16);
        // Check that it's not identity (camera is not at origin)
        assertThat(view).isNotEqualTo(MatrixUtils.identity());
    }

    @Test
    void lookAtFromOriginLookingDownZ() {
        float[] view = MatrixUtils.lookAt(0, 0, 10, 0, 0, 0, 0, 1, 0);
        // f = normalize(center - eye) = (0, 0, -1)
        // m[14] = dot(f, eye) = 0*0 + 0*0 + (-1)*10 = -10
        assertThat(view[14]).isCloseTo(-10f, within(0.001f));
    }

    @Test
    void perspectiveProducesDifferentAspectRatios() {
        float[] p1 = MatrixUtils.perspective(60, 1.0f, 0.1f, 100f);
        float[] p2 = MatrixUtils.perspective(60, 2.0f, 0.1f, 100f);
        // Different aspect ratios should produce different matrices
        assertThat(p1).isNotEqualTo(p2);
    }
}
