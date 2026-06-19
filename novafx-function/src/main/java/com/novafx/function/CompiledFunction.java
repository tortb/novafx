package com.novafx.function;

import java.util.Map;
import java.util.Objects;

/**
 * A compiled mathematical expression that has been parsed into an AST
 * and is ready for repeated evaluation.
 * <p>
 * Construction parses the expression immediately (fail-fast on syntax errors).
 * Subsequent {@link #evaluate} calls walk the AST without string processing.
 * <p>
 * Thread-safe: the AST is an immutable tree of records.
 *
 * <pre>
 * CompiledFunction f = new CompiledFunction("sin(t)");
 * double result = f.evaluate(0.0); // 0.0
 * </pre>
 */
public final class CompiledFunction {

    private final String source;
    private final AstNode ast;

    /**
     * Parses the expression string into an AST.
     *
     * @param expression the mathematical expression; must not be null or blank
     * @throws IllegalArgumentException if the expression is blank or contains syntax errors
     */
    public CompiledFunction(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression must not be null or blank");
        }
        this.source = expression;
        Tokenizer tokenizer = new Tokenizer(expression);
        AstParser parser = new AstParser(tokenizer);
        this.ast = parser.parse();
    }

    /**
     * Creates a compiled function from a pre-built AST.
     *
     * @param ast    the parsed AST; must not be null
     * @param source the original source string (may be empty)
     */
    public CompiledFunction(AstNode ast, String source) {
        this.ast = Objects.requireNonNull(ast, "ast must not be null");
        this.source = Objects.requireNonNullElse(source, "");
    }

    /**
     * Evaluates the function with the given variable bindings.
     *
     * @param variables variable name to value map
     * @return the computed result
     */
    public double evaluate(Map<String, Double> variables) {
        AstEvaluator evaluator = new AstEvaluator();
        return evaluator.evaluate(ast, variables);
    }

    /**
     * Evaluates the function with only the variable {@code t} bound.
     *
     * @param t the value of the parameter t
     * @return the computed result
     */
    public double evaluate(double t) {
        return evaluate(Map.of("t", t));
    }

    /**
     * Returns the parsed AST for inspection, transformation, or serialization.
     *
     * @return the root AST node
     */
    public AstNode ast() {
        return ast;
    }

    /**
     * Returns the original source expression string.
     *
     * @return the source string
     */
    public String source() {
        return source;
    }

    @Override
    public String toString() {
        return "CompiledFunction{'" + source + "'}";
    }
}
