package com.novafx.function;

import java.util.Map;

/**
 * Evaluates an {@link AstNode} tree to a numeric result given variable bindings.
 * <p>
 * Uses Java 25 exhaustive pattern matching on the sealed {@link AstNode} hierarchy.
 * Thread-safe: the evaluator holds no mutable state.
 */
public final class AstEvaluator {

    /**
     * Evaluates the AST node with the given variable bindings.
     *
     * @param node      the AST root; must not be null
     * @param variables variable name to value map; must not be null
     * @return the computed numeric result
     * @throws IllegalArgumentException if an unknown variable or function is encountered
     */
    public double evaluate(AstNode node, Map<String, Double> variables) {
        return switch (node) {
            case AstNode.ConstantNode c -> c.value();

            case AstNode.VariableNode v -> resolveVariable(v.name(), variables);

            case AstNode.UnaryNode(var operand, var op) -> {
                double val = evaluate(operand, variables);
                yield switch (op) {
                    case NEG -> -val;
                    case ABS -> Math.abs(val);
                };
            }

            case AstNode.BinaryNode(var left, var right, var op) -> {
                double l = evaluate(left, variables);
                double r = evaluate(right, variables);
                yield switch (op) {
                    case ADD -> l + r;
                    case SUB -> l - r;
                    case MUL -> l * r;
                    case DIV -> l / r; // IEEE 754: division by zero → +/-Infinity, not exception
                    case POW -> Math.pow(l, r);
                };
            }

            case AstNode.FunctionNode(var name, var args) -> {
                double[] evaluated = args.stream()
                        .mapToDouble(a -> evaluate(a, variables))
                        .toArray();
                yield applyBuiltin(name, evaluated);
            }
        };
    }

    /**
     * Evaluates with convenience single-variable binding.
     *
     * @param node the AST root
     * @param t    the value for variable "t"
     * @return the computed result
     */
    public double evaluate(AstNode node, double t) {
        return evaluate(node, Map.of("t", t));
    }

    // ---------------------------------------------------------------
    // Variable resolution
    // ---------------------------------------------------------------

    private static double resolveVariable(String name, Map<String, Double> variables) {
        // User-supplied variables take precedence
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        // Implicit constants
        return switch (name) {
            case "PI" -> Math.PI;
            case "E" -> Math.E;
            default -> throw new IllegalArgumentException("Unknown variable: '" + name + "'");
        };
    }

    // ---------------------------------------------------------------
    // Built-in function table
    // ---------------------------------------------------------------

    private static double applyBuiltin(String name, double[] args) {
        return switch (name) {
            case "sin"   -> { checkArgs(name, args, 1); yield Math.sin(args[0]); }
            case "cos"   -> { checkArgs(name, args, 1); yield Math.cos(args[0]); }
            case "tan"   -> { checkArgs(name, args, 1); yield Math.tan(args[0]); }
            case "sqrt"  -> { checkArgs(name, args, 1); yield Math.sqrt(args[0]); }
            case "abs"   -> { checkArgs(name, args, 1); yield Math.abs(args[0]); }
            case "log"   -> { checkArgs(name, args, 1); yield Math.log(args[0]); }
            case "exp"   -> { checkArgs(name, args, 1); yield Math.exp(args[0]); }
            case "floor" -> { checkArgs(name, args, 1); yield Math.floor(args[0]); }
            case "ceil"  -> { checkArgs(name, args, 1); yield Math.ceil(args[0]); }
            case "min"   -> { checkArgs(name, args, 2); yield Math.min(args[0], args[1]); }
            case "max"   -> { checkArgs(name, args, 2); yield Math.max(args[0], args[1]); }
            case "pow"   -> { checkArgs(name, args, 2); yield Math.pow(args[0], args[1]); }
            case "fourier" -> { checkArgs(name, args, 5); yield FourierSeries.evaluate(args[0], (int) args[1], args[2], args[3], args[4]); }
            default -> throw new IllegalArgumentException("Unknown function: '" + name + "'");
        };
    }

    private static double[] checkArgs(String name, double[] args, int expected) {
        if (args.length != expected) {
            throw new IllegalArgumentException(
                    "Function '" + name + "' expects " + expected
                            + " argument(s) but got " + args.length);
        }
        return args;
    }
}
