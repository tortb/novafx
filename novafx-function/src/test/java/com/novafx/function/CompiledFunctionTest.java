package com.novafx.function;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class CompiledFunctionTest {

    @Test
    void shouldEvaluateFullPipeline() {
        CompiledFunction f = new CompiledFunction("sin(t)");
        assertThat(f.evaluate(0.0)).isEqualTo(0.0);
        assertThat(f.evaluate(Math.PI / 2)).isCloseTo(1.0, within(1e-12));
    }

    @Test
    void shouldEvaluateWithVariableMap() {
        CompiledFunction f = new CompiledFunction("a+b");
        assertThat(f.evaluate(Map.of("a", 3.0, "b", 4.0))).isEqualTo(7.0);
    }

    @Test
    void shouldReuseAcrossCalls() {
        CompiledFunction f = new CompiledFunction("t*t");
        assertThat(f.evaluate(0)).isEqualTo(0);
        assertThat(f.evaluate(2)).isEqualTo(4);
        assertThat(f.evaluate(5)).isEqualTo(25);
    }

    @Test
    void shouldStoreSource() {
        CompiledFunction f = new CompiledFunction("cos(t)");
        assertThat(f.source()).isEqualTo("cos(t)");
    }

    @Test
    void shouldExposeAst() {
        CompiledFunction f = new CompiledFunction("42");
        assertThat(f.ast()).isInstanceOf(AstNode.ConstantNode.class);
    }

    @Test
    void shouldConstructFromAst() {
        AstNode ast = new AstNode.ConstantNode(99.0);
        CompiledFunction f = new CompiledFunction(ast, "99");
        assertThat(f.evaluate(Map.of())).isEqualTo(99.0);
        assertThat(f.source()).isEqualTo("99");
    }

    @Test
    void shouldRejectBlankExpression() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CompiledFunction(""));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CompiledFunction("   "));
    }

    @Test
    void shouldRejectNullExpression() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CompiledFunction((String) null));
    }

    @Test
    void shouldRejectInvalidExpression() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CompiledFunction("sin(+)"));
    }

    @Test
    void toStringShouldContainExpression() {
        CompiledFunction f = new CompiledFunction("t");
        assertThat(f.toString()).contains("t");
    }
}
