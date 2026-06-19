package com.novafx.function;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Extracts user-controllable parameter names from an AST.
 * <p>
 * Parameters are variable names that are <em>not</em> system-reserved
 * ({@code t, x, y, z}). These represent values the user can adjust
 * via sliders or other controls in the UI.
 * <p>
 * Example: for expression {@code "a * sin(b * t)"} the extractor returns
 * {@code {a, b}}.
 */
public final class ParameterExtractor {

    private static final Set<String> SYSTEM_VARS = Set.of("t", "x", "y", "z", "PI", "E");

    /**
     * Extracts user parameters from a parsed AST.
     *
     * @param node the AST root; must not be null
     * @return an immutable set of parameter names (never null, may be empty)
     */
    public Set<String> extract(AstNode node) {
        Set<String> result = new HashSet<>();
        traverse(node, result);
        result.removeAll(SYSTEM_VARS);
        return Collections.unmodifiableSet(result);
    }

    /**
     * Parses the expression and extracts parameters.
     *
     * @param expression a mathematical expression string
     * @return an immutable set of parameter names
     */
    public Set<String> extract(String expression) {
        CompiledFunction func = new CompiledFunction(expression);
        return extract(func.ast());
    }

    private static void traverse(AstNode node, Set<String> acc) {
        switch (node) {
            case AstNode.VariableNode v -> acc.add(v.name());
            case AstNode.BinaryNode b -> {
                traverse(b.left(), acc);
                traverse(b.right(), acc);
            }
            case AstNode.UnaryNode u -> traverse(u.operand(), acc);
            case AstNode.FunctionNode f -> f.arguments().forEach(a -> traverse(a, acc));
            case AstNode.ConstantNode ignored -> {}
        }
    }
}
