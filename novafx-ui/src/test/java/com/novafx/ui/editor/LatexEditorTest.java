package com.novafx.ui.editor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LatexEditorTest {

    @Test
    void shouldConvertSinCommand() {
        assertThat(LatexEditor.fromLatex("\\sin(t)")).isEqualTo("sin(t)");
    }

    @Test
    void shouldConvertCosCommand() {
        assertThat(LatexEditor.fromLatex("\\cos(t)")).isEqualTo("cos(t)");
    }

    @Test
    void shouldConvertSqrt() {
        assertThat(LatexEditor.fromLatex("\\sqrt{16}")).isEqualTo("sqrt(16)");
    }

    @Test
    void shouldConvertFrac() {
        assertThat(LatexEditor.fromLatex("\\frac{a}{b}")).isEqualTo("(a)/(b)");
    }

    @Test
    void shouldConvertComplexExpression() {
        String result = LatexEditor.fromLatex("\\frac{\\sin(t)}{2}");
        // After: remove braces → \frac{\sin(t)}{2} → (sin(t))/(2)
        assertThat(result).contains("sin(t)");
    }

    @Test
    void shouldConvertToLatex() {
        assertThat(LatexEditor.toLatex("sin(t)")).isEqualTo("\\sin(t)");
    }

    @Test
    void shouldNotDoubleEscape() {
        String result = LatexEditor.toLatex("\\sin(t)");
        // Should not add another backslash
        assertThat(result).isEqualTo("\\sin(t)");
    }

    @Test
    void shouldHandleNullInput() {
        assertThat(LatexEditor.fromLatex(null)).isNull();
    }

    @Test
    void shouldHandleEmptyInput() {
        assertThat(LatexEditor.fromLatex("")).isEqualTo("");
    }

    @Test
    void shouldConvertPow() {
        assertThat(LatexEditor.fromLatex("\\sin(t)+\\cos(t)"))
                .isEqualTo("sin(t)+cos(t)");
    }
}
