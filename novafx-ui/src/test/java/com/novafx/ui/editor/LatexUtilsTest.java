package com.novafx.ui.editor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LatexUtilsTest {

    @Test
    void shouldConvertSinCommand() {
        assertThat(LatexUtils.fromLatex("\\sin(t)")).isEqualTo("sin(t)");
    }

    @Test
    void shouldConvertCosCommand() {
        assertThat(LatexUtils.fromLatex("\\cos(t)")).isEqualTo("cos(t)");
    }

    @Test
    void shouldConvertSqrt() {
        assertThat(LatexUtils.fromLatex("\\sqrt{16}")).isEqualTo("sqrt(16)");
    }

    @Test
    void shouldConvertFrac() {
        assertThat(LatexUtils.fromLatex("\\frac{a}{b}")).isEqualTo("(a)/(b)");
    }

    @Test
    void shouldConvertComplexExpression() {
        String result = LatexUtils.fromLatex("\\frac{\\sin(t)}{2}");
        assertThat(result).contains("sin(t)");
    }

    @Test
    void shouldConvertToLatex() {
        assertThat(LatexUtils.toLatex("sin(t)")).isEqualTo("\\sin(t)");
    }

    @Test
    void shouldNotDoubleEscape() {
        String result = LatexUtils.toLatex("\\sin(t)");
        assertThat(result).isEqualTo("\\sin(t)");
    }

    @Test
    void shouldHandleNullInput() {
        assertThat(LatexUtils.fromLatex(null)).isNull();
    }

    @Test
    void shouldHandleEmptyInput() {
        assertThat(LatexUtils.fromLatex("")).isEqualTo("");
    }

    @Test
    void shouldConvertPow() {
        assertThat(LatexUtils.fromLatex("\\sin(t)+\\cos(t)"))
                .isEqualTo("sin(t)+cos(t)");
    }
}
