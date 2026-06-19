package com.novafx.ui.controller;

import com.novafx.function.Parameter;
import com.novafx.math.FunctionDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MainControllerTest {

    private MainController controller;

    @BeforeEach
    void setUp() {
        controller = new MainController();
    }

    @Test
    void shouldProvidePresetNames() {
        assertThat(controller.getPresetNames())
                .hasSize(12)
                .contains("Spiral", "Circle", "Heart", "DNA", "Sphere", "Torus");
    }

    @Test
    void shouldApplyPreset() {
        FunctionDefinition def = controller.applyPreset("Circle");
        assertThat(def).isNotNull();
        assertThat(def.xExpression()).isEqualTo("3*cos(t)");
    }

    @Test
    void shouldRejectUnknownPreset() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> controller.applyPreset("Unknown"));
    }

    @Test
    void shouldCreateNewProject() {
        controller.newProject();
        assertThat(controller.getCurrentDefinition()).isNotNull();
    }

    @Test
    void shouldUpdateFunction() {
        controller.newProject();
        controller.updateFunction("2*t", "t*t", "0", 0, 5, 0.5);
        FunctionDefinition def = controller.getCurrentDefinition();
        assertThat(def).isNotNull();
        assertThat(def.xExpression()).isEqualTo("2*t");
        assertThat(def.yExpression()).isEqualTo("t*t");
    }

    @Test
    void shouldProducePointsAfterApplyingPreset() {
        controller.applyPreset("Helix");
        assertThat(controller.getCurrentPoints()).isNotEmpty();
    }

    @Test
    void shouldProducePointsAfterUpdate() {
        controller.updateFunction("cos(t)", "sin(t)", "t/5", 0, 10, 0.1);
        assertThat(controller.getCurrentPoints()).isNotEmpty();
    }

    @Test
    void shouldInvokeOnStateChanged() {
        final boolean[] called = {false};
        controller.setOnStateChanged(() -> called[0] = true);
        controller.applyPreset("Star");
        assertThat(called[0]).isTrue();
    }

    @Test
    void shouldReturnEmptyPointsWithNoProject() {
        assertThat(controller.getCurrentPoints()).isEmpty();
    }

    // ---------------------------------------------------------------
    // Parameter tests
    // ---------------------------------------------------------------

    @Test
    void shouldExtractNoParamsForSimpleExpr() {
        controller.updateFunction("cos(t)", "sin(t)", "0", 0, 1, 0.1);
        assertThat(controller.getParameters()).isEmpty();
    }

    @Test
    void shouldExtractParamsForParametricExpr() {
        controller.updateFunction("a*sin(b*t)", "cos(t)", "0", 0, 1, 0.1);
        List<Parameter> params = controller.getParameters();
        assertThat(params).extracting(Parameter::name)
                .containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void shouldSetParameterAndResample() {
        controller.updateFunction("a*t", "t", "0", 0, 1, 1);
        assertThat(controller.getCurrentPoints()).isNotEmpty();

        controller.setParameter("a", 5.0);
        // At t=1, x should be a*t = 5*1 = 5
        List<com.novafx.math.Vector3d> points = controller.getCurrentPoints();
        assertThat(points.get(1).x()).isCloseTo(5.0, within(1e-10));
    }

    @Test
    void shouldInvokeOnStateChangedOnUpdate() {
        final boolean[] called = {false};
        controller.setOnStateChanged(() -> called[0] = true);
        controller.updateFunction("a*t", "t", "0", 0, 1, 0.5);
        assertThat(called[0]).isTrue();
    }

    @Test
    void shouldHaveDefaultValueOne() {
        controller.updateFunction("a*t", "t", "0", 0, 5, 1);
        List<Parameter> params = controller.getParameters();
        assertThat(params).isNotEmpty();
        assertThat(params.get(0).value()).isEqualTo(1.0);
    }

    @Test
    void shouldClearParamsOnNewProject() {
        controller.updateFunction("a*t", "t", "0", 0, 1, 1);
        assertThat(controller.getParameters()).isNotEmpty();

        controller.newProject();
        assertThat(controller.getParameters()).isEmpty();
    }
}
