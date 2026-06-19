package com.novafx.function;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AstPrinterTest {

    private final AstPrinter printer = new AstPrinter();

    private String print(String expr) {
        AstNode node = new AstParser(new Tokenizer(expr)).parse();
        return printer.print(node);
    }

    @Test
    void shouldPrintConstant() {
        assertThat(print("42")).isEqualTo("42");
    }

    @Test
    void shouldPrintVariable() {
        assertThat(print("t")).isEqualTo("t");
    }

    @Test
    void shouldPrintBinaryExpr() {
        assertThat(print("2+3")).isEqualTo("(2 + 3)");
    }

    @Test
    void shouldPrintNestedBinary() {
        assertThat(print("2+3*4")).isEqualTo("(2 + (3 * 4))");
    }

    @Test
    void shouldPrintUnaryMinus() {
        assertThat(print("-5")).isEqualTo("(-5)");
    }

    @Test
    void shouldPrintFunctionCall() {
        assertThat(print("sin(t)")).isEqualTo("sin(t)");
    }

    @Test
    void shouldPrintFunctionWithMultipleArgs() {
        assertThat(print("pow(2,10)")).isEqualTo("pow(2, 10)");
    }

    @Test
    void shouldPrintNestedFunctions() {
        assertThat(print("sqrt(abs(-16))")).isEqualTo("sqrt(abs((-16)))");
    }

    @Test
    void shouldPrintPowerRightAssoc() {
        assertThat(print("2^3^2")).isEqualTo("(2 ^ (3 ^ 2))");
    }
}
