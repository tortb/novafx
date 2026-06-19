package com.novafx.function;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AstParserTest {

    private AstNode parse(String expr) {
        return new AstParser(new Tokenizer(expr)).parse();
    }

    @Test
    void shouldParseConstant() {
        AstNode node = parse("42");
        assertThat(node).isInstanceOf(AstNode.ConstantNode.class);
        assertThat(((AstNode.ConstantNode) node).value()).isEqualTo(42);
    }

    @Test
    void shouldParseVariable() {
        AstNode node = parse("t");
        assertThat(node).isInstanceOf(AstNode.VariableNode.class);
        assertThat(((AstNode.VariableNode) node).name()).isEqualTo("t");
    }

    @Test
    void shouldParseAddition() {
        AstNode node = parse("2+3");
        assertThat(node).isInstanceOf(AstNode.BinaryNode.class);
        var b = (AstNode.BinaryNode) node;
        assertThat(b.op()).isEqualTo(AstNode.BinaryOp.ADD);
        assertThat(b.left()).isEqualTo(new AstNode.ConstantNode(2));
        assertThat(b.right()).isEqualTo(new AstNode.ConstantNode(3));
    }

    @Test
    void shouldParseSubtraction() {
        AstNode node = parse("5-3");
        assertThat(((AstNode.BinaryNode) node).op()).isEqualTo(AstNode.BinaryOp.SUB);
    }

    @Test
    void shouldParseMultiplication() {
        AstNode node = parse("4*5");
        assertThat(((AstNode.BinaryNode) node).op()).isEqualTo(AstNode.BinaryOp.MUL);
    }

    @Test
    void shouldParseDivision() {
        AstNode node = parse("10/2");
        assertThat(((AstNode.BinaryNode) node).op()).isEqualTo(AstNode.BinaryOp.DIV);
    }

    @Test
    void shouldParseExponentiation() {
        AstNode node = parse("2^3");
        assertThat(((AstNode.BinaryNode) node).op()).isEqualTo(AstNode.BinaryOp.POW);
    }

    @Test
    void shouldRespectPrecedenceMulBeforeAdd() {
        // 2+3*4 -> 2+(3*4)
        AstNode node = parse("2+3*4");
        assertThat(node).isInstanceOf(AstNode.BinaryNode.class);
        var add = (AstNode.BinaryNode) node;
        assertThat(add.op()).isEqualTo(AstNode.BinaryOp.ADD);
        assertThat(add.left()).isEqualTo(new AstNode.ConstantNode(2));
        assertThat(add.right()).isInstanceOf(AstNode.BinaryNode.class);
        var mul = (AstNode.BinaryNode) add.right();
        assertThat(mul.op()).isEqualTo(AstNode.BinaryOp.MUL);
    }

    @Test
    void shouldRespectPrecedenceAddBeforeMul() {
        // 3*4+2 -> (3*4)+2
        AstNode node = parse("3*4+2");
        var add = (AstNode.BinaryNode) node;
        assertThat(add.op()).isEqualTo(AstNode.BinaryOp.ADD);
        assertThat(add.left()).isInstanceOf(AstNode.BinaryNode.class);
        assertThat(((AstNode.BinaryNode) add.left()).op()).isEqualTo(AstNode.BinaryOp.MUL);
    }

    @Test
    void shouldHandleRightAssociativePower() {
        // 2^3^2 -> 2^(3^2)
        AstNode node = parse("2^3^2");
        var outer = (AstNode.BinaryNode) node;
        assertThat(outer.op()).isEqualTo(AstNode.BinaryOp.POW);
        assertThat(outer.left()).isEqualTo(new AstNode.ConstantNode(2));
        var inner = (AstNode.BinaryNode) outer.right();
        assertThat(inner.op()).isEqualTo(AstNode.BinaryOp.POW);
        assertThat(inner.left()).isEqualTo(new AstNode.ConstantNode(3));
        assertThat(inner.right()).isEqualTo(new AstNode.ConstantNode(2));
    }

    @Test
    void shouldParseUnaryMinus() {
        AstNode node = parse("-5");
        assertThat(node).isInstanceOf(AstNode.UnaryNode.class);
        assertThat(((AstNode.UnaryNode) node).op()).isEqualTo(AstNode.UnaryOp.NEG);
        assertThat(((AstNode.UnaryNode) node).operand()).isEqualTo(new AstNode.ConstantNode(5));
    }

    @Test
    void shouldParseDoubleUnaryMinus() {
        AstNode node = parse("--5");
        assertThat(node).isInstanceOf(AstNode.UnaryNode.class);
        var outer = (AstNode.UnaryNode) node;
        assertThat(outer.op()).isEqualTo(AstNode.UnaryOp.NEG);
        var inner = (AstNode.UnaryNode) outer.operand();
        assertThat(inner.op()).isEqualTo(AstNode.UnaryOp.NEG);
        assertThat(inner.operand()).isEqualTo(new AstNode.ConstantNode(5));
    }

    @Test
    void shouldParseUnaryPlusAsNoOp() {
        AstNode node = parse("+5");
        assertThat(node).isEqualTo(new AstNode.ConstantNode(5));
    }

    @Test
    void shouldParseFunctionCall() {
        AstNode node = parse("sin(t)");
        assertThat(node).isInstanceOf(AstNode.FunctionNode.class);
        var fn = (AstNode.FunctionNode) node;
        assertThat(fn.name()).isEqualTo("sin");
        assertThat(fn.arguments()).hasSize(1);
        assertThat(fn.arguments().get(0)).isEqualTo(new AstNode.VariableNode("t"));
    }

    @Test
    void shouldParseFunctionWithMultipleArgs() {
        AstNode node = parse("pow(2,10)");
        var fn = (AstNode.FunctionNode) node;
        assertThat(fn.name()).isEqualTo("pow");
        assertThat(fn.arguments()).hasSize(2);
    }

    @Test
    void shouldParseNestedFunctions() {
        AstNode node = parse("sqrt(abs(-16))");
        var outer = (AstNode.FunctionNode) node;
        assertThat(outer.name()).isEqualTo("sqrt");
        var inner = (AstNode.FunctionNode) outer.arguments().get(0);
        assertThat(inner.name()).isEqualTo("abs");
    }

    @Test
    void shouldHandleParentheses() {
        AstNode node = parse("(2+3)*4");
        var mul = (AstNode.BinaryNode) node;
        assertThat(mul.op()).isEqualTo(AstNode.BinaryOp.MUL);
        assertThat(mul.left()).isInstanceOf(AstNode.BinaryNode.class);
        assertThat(((AstNode.BinaryNode) mul.left()).op()).isEqualTo(AstNode.BinaryOp.ADD);
        assertThat(mul.right()).isEqualTo(new AstNode.ConstantNode(4));
    }

    @Test
    void shouldRejectMismatchedParentheses() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("(2+3"))
                .withMessageContaining(")");
    }

    @Test
    void shouldRejectTrailingOperator() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("2+"));
    }

    @Test
    void shouldRejectEmptyExpression() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(""));
    }

    @Test
    void shouldRejectExtraTokens() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse("2 3"));
    }

    @Test
    void shouldParseComplexExpression() {
        // 16*pow(sin(t),3)
        AstNode node = parse("16*pow(sin(t),3)");
        assertThat(node).isNotNull();
    }

    @Test
    void shouldParseNegatedExpression() {
        AstNode node = parse("-(2+3)");
        var unary = (AstNode.UnaryNode) node;
        assertThat(unary.op()).isEqualTo(AstNode.UnaryOp.NEG);
        assertThat(((AstNode.BinaryNode) unary.operand()).op()).isEqualTo(AstNode.BinaryOp.ADD);
    }
}
