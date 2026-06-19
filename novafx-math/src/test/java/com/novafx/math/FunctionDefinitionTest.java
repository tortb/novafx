package com.novafx.math;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FunctionDefinitionTest {

    @Test
    void shouldCreateValidDefinition() {
        FunctionDefinition def = new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 2 * Math.PI, 0.1);
        assertThat(def.xExpression()).isEqualTo("cos(t)");
        assertThat(def.yExpression()).isEqualTo("sin(t)");
        assertThat(def.zExpression()).isEqualTo("0");
        assertThat(def.start()).isEqualTo(0);
        assertThat(def.end()).isCloseTo(2 * Math.PI, within(1e-10));
        assertThat(def.step()).isEqualTo(0.1);
    }

    @Test
    void shouldRejectNullExpressions() {
        assertThatNullPointerException()
                .isThrownBy(() -> new FunctionDefinition(null, "sin(t)", "0", 0, 1, 0.1));
        assertThatNullPointerException()
                .isThrownBy(() -> new FunctionDefinition("cos(t)", null, "0", 0, 1, 0.1));
        assertThatNullPointerException()
                .isThrownBy(() -> new FunctionDefinition("cos(t)", "sin(t)", null, 0, 1, 0.1));
    }

    @Test
    void shouldRejectNonPositiveStep() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 1, 0));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 1, -0.1));
    }

    @Test
    void shouldComputeSampleCount() {
        FunctionDefinition def = new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 1, 0.25);
        assertThat(def.sampleCount()).isEqualTo(5); // t=0, 0.25, 0.5, 0.75, 1.0
    }

    @Test
    void shouldComputeSampleCountForExactDivision() {
        FunctionDefinition def = new FunctionDefinition("t", "t", "t", 0, 10, 1);
        assertThat(def.sampleCount()).isEqualTo(11);
    }

    @Test
    void shouldReturnZeroSampleCountWhenStartExceedsEnd() {
        FunctionDefinition def = new FunctionDefinition("t", "t", "t", 5, 3, 0.1);
        assertThat(def.sampleCount()).isEqualTo(0);
    }

    @Test
    void shouldHaveCorrectEquality() {
        FunctionDefinition a = new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 1, 0.1);
        FunctionDefinition b = new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 1, 0.1);
        FunctionDefinition c = new FunctionDefinition("sin(t)", "cos(t)", "0", 0, 1, 0.1);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    void shouldHandleLargeSampleCount() {
        FunctionDefinition def = new FunctionDefinition("t", "t", "t", 0, 1000, 0.001);
        assertThat(def.sampleCount()).isEqualTo(1_000_001L);
    }

    @Test
    void toStringShouldContainExpressions() {
        FunctionDefinition def = new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 1, 0.1);
        assertThat(def.toString()).contains("cos(t)", "sin(t)");
    }

    @Test
    void parameterNamesShouldBeEmptyForSimpleExpressions() {
        FunctionDefinition def = new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 1, 0.1);
        assertThat(def.parameterNames()).isEmpty();
    }

    @Test
    void parameterNamesShouldExtractCustomVars() {
        FunctionDefinition def = new FunctionDefinition("a*sin(b*t)", "c*cos(d*t)", "0", 0, 1, 0.1);
        assertThat(def.parameterNames()).containsExactlyInAnyOrder("a", "b", "c", "d");
    }

    @Test
    void parameterNamesShouldExcludeSystemVars() {
        FunctionDefinition def = new FunctionDefinition("x+y+z+t", "t", "0", 0, 1, 0.1);
        assertThat(def.parameterNames()).isEmpty();
    }

    @Test
    void parameterNamesShouldExcludeConstants() {
        FunctionDefinition def = new FunctionDefinition("PI+E", "t", "0", 0, 1, 0.1);
        assertThat(def.parameterNames()).isEmpty();
    }

    @Test
    void parameterNamesShouldUnionAllAxes() {
        FunctionDefinition def = new FunctionDefinition("a*t", "b*t", "c*t", 0, 1, 0.1);
        assertThat(def.parameterNames()).containsExactlyInAnyOrder("a", "b", "c");
    }
}
