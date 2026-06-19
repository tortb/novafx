package com.novafx.function;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class AstEvaluatorTest {

    private final AstEvaluator evaluator = new AstEvaluator();

    private double eval(String expr, Map<String, Double> vars) {
        AstNode node = new AstParser(new Tokenizer(expr)).parse();
        return evaluator.evaluate(node, vars);
    }

    private double eval(String expr) {
        return eval(expr, Map.of());
    }

    private double evalT(String expr, double t) {
        return eval(expr, Map.of("t", t));
    }

    @Test
    void constant() {
        assertThat(eval("42")).isEqualTo(42);
    }

    @Test
    void variableT() {
        assertThat(evalT("t", 5)).isEqualTo(5);
    }

    @Test
    void addition() {
        assertThat(evalT("t+3", 2)).isEqualTo(5);
    }

    @Test
    void subtraction() {
        assertThat(eval("5-3")).isEqualTo(2);
    }

    @Test
    void multiplication() {
        assertThat(eval("4*5")).isEqualTo(20);
    }

    @Test
    void division() {
        assertThat(eval("10/4")).isEqualTo(2.5);
    }

    @Test
    void power() {
        assertThat(eval("2^10")).isEqualTo(1024);
    }

    @Test
    void unaryNegation() {
        assertThat(eval("-5")).isEqualTo(-5);
    }

    @Test
    void doubleNegation() {
        assertThat(eval("--5")).isEqualTo(5);
    }

    @Test
    void sin() {
        assertThat(eval("sin(0)")).isEqualTo(0);
    }

    @Test
    void cos() {
        assertThat(eval("cos(0)")).isEqualTo(1);
    }

    @Test
    void tan() {
        assertThat(eval("tan(0)")).isEqualTo(0);
    }

    @Test
    void sqrt() {
        assertThat(eval("sqrt(16)")).isEqualTo(4);
    }

    @Test
    void abs() {
        assertThat(eval("abs(-5)")).isEqualTo(5);
    }

    @Test
    void log() {
        assertThat(eval("log(1)")).isEqualTo(0);
    }

    @Test
    void logE() {
        assertThat(eval("log(E)")).isCloseTo(1.0, within(1e-12));
    }

    @Test
    void exp() {
        assertThat(eval("exp(0)")).isEqualTo(1);
    }

    @Test
    void min() {
        assertThat(eval("min(3,7)")).isEqualTo(3);
    }

    @Test
    void max() {
        assertThat(eval("max(3,7)")).isEqualTo(7);
    }

    @Test
    void pow() {
        assertThat(eval("pow(2,10)")).isEqualTo(1024);
    }

    @Test
    void floor() {
        assertThat(eval("floor(3.7)")).isEqualTo(3);
    }

    @Test
    void ceil() {
        assertThat(eval("ceil(3.2)")).isEqualTo(4);
    }

    @Test
    void pi() {
        assertThat(eval("PI")).isCloseTo(Math.PI, within(1e-12));
    }

    @Test
    void e() {
        assertThat(eval("E")).isCloseTo(Math.E, within(1e-12));
    }

    @Test
    void precedenceMultiplicationBeforeAddition() {
        assertThat(eval("2+3*4")).isEqualTo(14);
    }

    @Test
    void precedencePowerRightAssociative() {
        // 2^3^2 = 2^(3^2) = 2^9 = 512
        assertThat(eval("2^3^2")).isEqualTo(512);
    }

    @Test
    void parenthesesOverridePrecedence() {
        assertThat(eval("(2+3)*4")).isEqualTo(20);
    }

    @Test
    void complexExpression() {
        assertThat(evalT("16*pow(sin(t),3)", Math.PI / 2)).isEqualTo(16);
    }

    @Test
    void divisionByZeroYieldsInfinity() {
        assertThat(eval("1/0")).isEqualTo(Double.POSITIVE_INFINITY);
    }

    @Test
    void unknownVariable() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> eval("unknown"))
                .withMessageContaining("Unknown variable");
    }

    @Test
    void unknownFunction() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> eval("bogus(0)"))
                .withMessageContaining("Unknown function");
    }

    @Test
    void functionWrongArgCount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> eval("sin(1,2)"))
                .withMessageContaining("expects 1");
    }

    @Test
    void multipleVariables() {
        assertThat(eval("x+y+z", Map.of("x", 1.0, "y", 2.0, "z", 3.0))).isEqualTo(6);
    }

    @Test
    void complexParametric() {
        // a*sin(b*t)
        double result = eval("a*sin(b*t)", Map.of("a", 2.0, "b", 3.0, "t", Math.PI / 6));
        assertThat(result).isCloseTo(2 * Math.sin(3 * Math.PI / 6), within(1e-12));
    }

    @Test
    void nestedNegation() {
        assertThat(eval("-(-5)")).isEqualTo(5);
    }

    @Test
    void evaluateWithConvenienceMethod() {
        AstNode node = new AstParser(new Tokenizer("t*t")).parse();
        assertThat(evaluator.evaluate(node, 5)).isEqualTo(25);
    }

    @Test
    void fourierBuiltin() {
        // fourier(t, 1, 1, 0, 1) = sin(t)
        assertThat(evalT("fourier(t,1,1,0,1)", 0)).isEqualTo(0);
        assertThat(evalT("fourier(t,1,1,0,1)", Math.PI / 2))
                .isCloseTo(1.0, within(1e-10));
    }

    @Test
    void fourierBuiltinMultipleHarmonics() {
        // fourier(t, 3, 1, 0, 1) = sin(t) + sin(2t) + sin(3t)
        double t = 0.7;
        double expected = Math.sin(t) + Math.sin(2 * t) + Math.sin(3 * t);
        assertThat(evalT("fourier(t,3,1,0,1)", t))
                .isCloseTo(expected, within(1e-10));
    }

    @Test
    void fourierBuiltinWrongArgCount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> eval("fourier(t,1,1,0)"));
    }

    @Test
    void fourierWithDecayAndAmplitude() {
        // fourier(t, 3, 2, 0.5, 2)
        double t = 0.7;
        double expected = 2 * (Math.sin(2 * t) + Math.sin(4 * t) / Math.pow(2, 0.5) + Math.sin(6 * t) / Math.pow(3, 0.5));
        assertThat(evalT("fourier(t,3,2,0.5,2)", t))
                .isCloseTo(expected, within(1e-10));
    }
}
