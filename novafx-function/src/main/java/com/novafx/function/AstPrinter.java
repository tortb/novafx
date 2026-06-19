package com.novafx.function;

import java.util.stream.Collectors;

/**
 * Prints an {@link AstNode} tree as a human-readable string.
 * <p>
 * Binary operations are always parenthesized to make the tree structure
 * unambiguous. The output is itself a valid expression that can be parsed.
 */
public final class AstPrinter {

    /**
     * Converts the AST to a string representation.
     *
     * @param node the AST root; must not be null
     * @return a string representation of the expression
     */
    public String print(AstNode node) {
        return switch (node) {
            case AstNode.ConstantNode c -> formatNumber(c.value());

            case AstNode.VariableNode v -> v.name();

            case AstNode.UnaryNode(var operand, var op) -> switch (op) {
                case NEG -> "(-" + print(operand) + ")";
                case ABS -> "(abs " + print(operand) + ")";
            };

            case AstNode.BinaryNode(var left, var right, var op) ->
                    "(" + print(left) + " " + opSymbol(op) + " " + print(right) + ")";

            case AstNode.FunctionNode(var name, var args) ->
                    name + "(" + args.stream().map(this::print).collect(Collectors.joining(", ")) + ")";
        };
    }

    private static String opSymbol(AstNode.BinaryOp op) {
        return switch (op) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "*";
            case DIV -> "/";
            case POW -> "^";
        };
    }

    private static String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }
}
