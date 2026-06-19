package com.novafx.math;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class MathPresetsTest {

    @Test
    void shouldProvideAllNinePresets() {
        Map<String, FunctionDefinition> all = MathPresets.all();
        assertThat(all).hasSize(9);
    }

    @Test
    void shouldContainAllRequiredPresets() {
        assertThat(MathPresets.names()).containsExactly(
                "Circle", "Heart", "Star", "Spiral", "DoubleSpiral",
                "Infinity", "Flower", "Wave", "Helix"
        );
    }

    @Test
    void shouldReturnDefinitionByName() {
        FunctionDefinition circle = MathPresets.byName("Circle");
        assertThat(circle).isNotNull();
        assertThat(circle.xExpression()).isEqualTo("3*cos(t)");
        assertThat(circle.yExpression()).isEqualTo("3*sin(t)");
    }

    @Test
    void shouldReturnNullForUnknownName() {
        assertThat(MathPresets.byName("Unknown")).isNull();
    }

    @Test
    void shouldReturnImmutableNamesList() {
        List<String> names = MathPresets.names();
        assertThatThrownBy(() -> names.add("Extra"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void circleShouldHavePositiveSampleCount() {
        assertThat(MathPresets.circle().sampleCount()).isPositive();
    }

    @Test
    void heartShouldHavePositiveSampleCount() {
        assertThat(MathPresets.heart().sampleCount()).isPositive();
    }

    @Test
    void starShouldHavePositiveSampleCount() {
        assertThat(MathPresets.star().sampleCount()).isPositive();
    }

    @Test
    void spiralShouldHavePositiveSampleCount() {
        assertThat(MathPresets.spiral().sampleCount()).isPositive();
    }

    @Test
    void doubleSpiralShouldHavePositiveSampleCount() {
        assertThat(MathPresets.doubleSpiral().sampleCount()).isPositive();
    }

    @Test
    void infinityShouldHavePositiveSampleCount() {
        assertThat(MathPresets.infinity().sampleCount()).isPositive();
    }

    @Test
    void flowerShouldHavePositiveSampleCount() {
        assertThat(MathPresets.flower().sampleCount()).isPositive();
    }

    @Test
    void waveShouldHavePositiveSampleCount() {
        assertThat(MathPresets.wave().sampleCount()).isPositive();
    }

    @Test
    void helixShouldHavePositiveSampleCount() {
        assertThat(MathPresets.helix().sampleCount()).isPositive();
    }

    @Test
    void circleShouldBeFlat() {
        assertThat(MathPresets.circle().zExpression()).isEqualTo("0");
    }

    @Test
    void helixShouldBe3D() {
        FunctionDefinition helix = MathPresets.helix();
        assertThat(helix.zExpression()).isEqualTo("t/3");
        assertThat(helix.xExpression()).isEqualTo("3*cos(t)");
        assertThat(helix.yExpression()).isEqualTo("3*sin(t)");
    }

    @Test
    void presetsShouldBeSampleable() {
        AstFunctionSampler sampler = new AstFunctionSampler();
        for (String name : MathPresets.names()) {
            FunctionDefinition def = MathPresets.byName(name);
            assertThat(sampler.sample(def)).isNotEmpty();
        }
    }
}
