package com.novafx.math;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class Vector3dTest {

    @Test
    void shouldCreateVectorWithGivenCoordinates() {
        Vector3d v = new Vector3d(1.0, 2.0, 3.0);
        assertThat(v.x()).isEqualTo(1.0);
        assertThat(v.y()).isEqualTo(2.0);
        assertThat(v.z()).isEqualTo(3.0);
    }

    @Test
    void shouldComputeNorm() {
        Vector3d v = new Vector3d(3.0, 4.0, 0.0);
        assertThat(v.norm()).isEqualTo(5.0);
    }

    @Test
    void shouldComputeNormOfZeroVector() {
        assertThat(Vector3d.ZERO.norm()).isEqualTo(0.0);
    }

    @Test
    void shouldScaleByScalar() {
        Vector3d v = new Vector3d(1.0, 2.0, 3.0);
        Vector3d scaled = v.multiply(2.0);
        assertThat(scaled).isEqualTo(new Vector3d(2.0, 4.0, 6.0));
    }

    @Test
    void shouldScaleZeroVector() {
        Vector3d scaled = Vector3d.ZERO.multiply(5.0);
        assertThat(scaled).isEqualTo(Vector3d.ZERO);
    }

    @Test
    void shouldAddVectors() {
        Vector3d a = new Vector3d(1.0, 2.0, 3.0);
        Vector3d b = new Vector3d(4.0, 5.0, 6.0);
        assertThat(a.add(b)).isEqualTo(new Vector3d(5.0, 7.0, 9.0));
    }

    @Test
    void shouldSubtractVectors() {
        Vector3d a = new Vector3d(5.0, 7.0, 9.0);
        Vector3d b = new Vector3d(1.0, 2.0, 3.0);
        assertThat(a.subtract(b)).isEqualTo(new Vector3d(4.0, 5.0, 6.0));
    }

    @Test
    void shouldBeImmutable() {
        Vector3d v = new Vector3d(1.0, 2.0, 3.0);
        assertThat(v.x()).isEqualTo(1.0);
        assertThat(v.y()).isEqualTo(2.0);
        assertThat(v.z()).isEqualTo(3.0);
    }

    @Test
    void shouldHaveCorrectEquality() {
        Vector3d a = new Vector3d(1.0, 2.0, 3.0);
        Vector3d b = new Vector3d(1.0, 2.0, 3.0);
        Vector3d c = new Vector3d(1.0, 2.0, 4.0);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    void shouldHaveZeroNormForOrigin() {
        assertThat(new Vector3d(0, 0, 0).norm()).isEqualTo(0.0);
    }

    @Test
    void shouldScaleWithZero() {
        Vector3d v = new Vector3d(2, 3, 4);
        assertThat(v.multiply(0)).isEqualTo(Vector3d.ZERO);
    }

    @Test
    void shouldAddToZeroProduceSelf() {
        Vector3d v = new Vector3d(2, 3, 4);
        assertThat(v.add(Vector3d.ZERO)).isEqualTo(v);
    }

    @Test
    void shouldSubtractSelfProduceZero() {
        Vector3d v = new Vector3d(2, 3, 4);
        assertThat(v.subtract(v)).isEqualTo(Vector3d.ZERO);
    }

    @Test
    void toStringShouldContainComponents() {
        Vector3d v = new Vector3d(1.5, -2.5, 3.0);
        assertThat(v.toString()).contains("1.5", "-2.5", "3.0");
    }
}
