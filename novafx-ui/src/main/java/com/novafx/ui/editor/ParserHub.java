package com.novafx.ui.editor;

import com.novafx.function.CompiledFunction;
import com.novafx.function.ParameterExtractor;
import com.novafx.math.FunctionDefinition;

import java.util.Set;

/**
 * Unified parser hub that routes input from all editor modes
 * to the Function AST pipeline.
 * <p>
 * All three editor modes (Simple, Expression, LaTeX) ultimately
 * produce {@link FunctionDefinition} through this hub.
 */
public final class ParserHub {

    private final ParameterExtractor parameterExtractor = new ParameterExtractor();

    /** Input mode type. */
    public enum InputMode {
        SIMPLE,
        EXPRESSION,
        LATEX
    }

    /**
     * Parses three coordinate expressions into a {@link FunctionDefinition}.
     * <p>
     * Validates each expression by attempting to compile it.
     *
     * @param xExpr x-coordinate expression
     * @param yExpr y-coordinate expression
     * @param zExpr z-coordinate expression
     * @param start parameter start value
     * @param end   parameter end value
     * @param step  sampling step
     * @return a validated FunctionDefinition
     * @throws IllegalArgumentException if any expression fails to compile
     */
    public FunctionDefinition parse(String xExpr, String yExpr, String zExpr,
                                    double start, double end, double step) {
        // Validate by attempting compilation
        new CompiledFunction(xExpr);
        new CompiledFunction(yExpr);
        new CompiledFunction(zExpr);

        return new FunctionDefinition(xExpr, yExpr, zExpr, start, end, step);
    }

    /**
     * Extracts parameter names from coordinate expressions.
     *
     * @param xExpr x-expression
     * @param yExpr y-expression
     * @param zExpr z-expression
     * @return set of parameter names excluding system vars
     */
    public Set<String> extractParameters(String xExpr, String yExpr, String zExpr) {
        Set<String> params = parameterExtractor.extract(xExpr);
        params.addAll(parameterExtractor.extract(yExpr));
        params.addAll(parameterExtractor.extract(zExpr));
        return params;
    }
}
