package com.novafx.function;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class ParameterExtractorTest {

    private final ParameterExtractor extractor = new ParameterExtractor();

    @Test
    void shouldExtractCustomParameters() {
        assertThat(extractor.extract("a*sin(b*t)")).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void shouldExcludeSystemVariableT() {
        assertThat(extractor.extract("3*cos(t)")).isEmpty();
    }

    @Test
    void shouldExcludeXYZ() {
        assertThat(extractor.extract("x+y+z")).isEmpty();
    }

    @Test
    void shouldExcludePI() {
        // PI is an implicit constant, not a parameter
        assertThat(extractor.extract("a+PI")).containsExactly("a");
    }

    @Test
    void shouldExcludeE() {
        assertThat(extractor.extract("E*a")).containsExactly("a");
    }

    @Test
    void shouldDeduplicate() {
        assertThat(extractor.extract("a+b+a")).containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void shouldExtractFromNestedFunctions() {
        assertThat(extractor.extract("sin(a*t)+cos(b*t)"))
                .containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void shouldExtractFromComplexExpression() {
        assertThat(extractor.extract("a*pow(sin(b*t),c)"))
                .containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    void shouldHandleEmptyExpression() {
        assertThat(extractor.extract("42")).isEmpty();
    }

    @Test
    void shouldHandleSingleSystemVar() {
        assertThat(extractor.extract("t")).isEmpty();
    }

    @Test
    void shouldExtractFromUnaryExpression() {
        assertThat(extractor.extract("-a")).containsExactly("a");
    }

    @Test
    void shouldReturnImmutableSet() {
        Set<String> params = extractor.extract("a*t");
        assertThatThrownBy(() -> params.add("b"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
