package com.novafx.function;

import java.util.List;

/**
 * Root type of the NovaFX expression AST (Abstract Syntax Tree).
 * <p>
 * A parsed mathematical expression is represented as a tree of {@code AstNode}
 * instances. The sealed hierarchy ensures exhaustive pattern matching in
 * Java 25 {@code switch} expressions.
 * <p>
 * Node types:
 * <ul>
 *   <li>{@link ConstantNode} — literal numbers (e.g. {@code 42}, {@code 3.14})</li>
 *   <li>{@link VariableNode} — named variables (e.g. {@code t}, {@code a})</li>
 *   <li>{@link UnaryNode} — unary operations ({@code -x})</li>
 *   <li>{@link BinaryNode} — binary operations ({@code a+b})</li>
 *   <li>{@link FunctionNode} — function calls ({@code sin(t)}, {@code max(a,b)})</li>
 * </ul>
 */
public sealed interface AstNode {

    /**
     * A literal numeric constant.
     *
     * @param value the constant value as a primitive double
     */
    record ConstantNode(double value) implements AstNode {}

    /**
     * A named variable reference.
     *
     * @param name the variable name (e.g. "t", "a", "PI")
     */
    record VariableNode(String name) implements AstNode {}

    /**
     * A unary operation applied to a single operand.
     *
     * @param operand the inner expression
     * @param op      the unary operator
     */
    record UnaryNode(AstNode operand, UnaryOp op) implements AstNode {}

    /**
     * A binary operation applied to two operands.
     *
     * @param left  the left-hand side expression
     * @param right the right-hand side expression
     * @param op    the binary operator
     */
    record BinaryNode(AstNode left, AstNode right, BinaryOp op) implements AstNode {}

    /**
     * A function call with a name and zero or more arguments.
     *
     * @param name      the function name (e.g. "sin", "pow")
     * @param arguments the argument expressions
     */
    record FunctionNode(String name, List<AstNode> arguments) implements AstNode {}

    // ---------------------------------------------------------------
    // Operator enums
    // ---------------------------------------------------------------

    /** Binary arithmetic operators in order of increasing precedence. */
    enum BinaryOp {
        ADD,
        SUB,
        MUL,
        DIV,
        POW
    }

    /** Unary operators. */
    enum UnaryOp {
        /** Arithmetic negation ({@code -x}). */
        NEG,
        /** Absolute value ({@code abs(x)} — produced by the parser for the abs function or implicit). */
        ABS
    }
}
