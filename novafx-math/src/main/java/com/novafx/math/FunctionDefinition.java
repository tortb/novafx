package com.novafx.math;

import com.novafx.function.CompiledFunction;
import com.novafx.function.ParameterExtractor;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Defines a parametric function in 3D space: {@code x = f(t)}, {@code y = g(t)}, {@code z = h(t)}.
 * <p>
 * The domain of the parameter {@code t} is {@code [start, end]} and samples are taken
 * at increments of {@code step}.
 * <p>
 * Expressions are validated and pre-compiled into an AST at construction time
 * (fail-fast on syntax errors). Use {@link #xCompiled()}, {@link #yCompiled()},
 * {@link #zCompiled()} for repeated evaluation without re-parsing.
 */
public final class FunctionDefinition {

    private final String xExpression;
    private final String yExpression;
    private final String zExpression;
    private final double start;
    private final double end;
    private final double step;

    private final transient CompiledFunction xCompiled;
    private final transient CompiledFunction yCompiled;
    private final transient CompiledFunction zCompiled;

    /**
     * Constructs a fully specified parametric function definition.
     * <p>
     * Each expression is parsed immediately to validate its syntax.
     *
     * @param xExpression expression for the x-coordinate in terms of {@code t}
     * @param yExpression expression for the y-coordinate in terms of {@code t}
     * @param zExpression expression for the z-coordinate in terms of {@code t}
     * @param start       lower bound of the parameter {@code t}
     * @param end         upper bound of the parameter {@code t}
     * @param step        sampling increment; must be positive
     * @throws IllegalArgumentException if any expression is syntactically invalid
     *                                  or {@code step <= 0}
     * @throws NullPointerException     if any expression is null
     */
    public FunctionDefinition(String xExpression, String yExpression, String zExpression,
                              double start, double end, double step) {
        this.xExpression = Objects.requireNonNull(xExpression, "xExpression must not be null");
        this.yExpression = Objects.requireNonNull(yExpression, "yExpression must not be null");
        this.zExpression = Objects.requireNonNull(zExpression, "zExpression must not be null");
        if (step <= 0) {
            throw new IllegalArgumentException("step must be positive, got: " + step);
        }
        this.start = start;
        this.end = end;
        this.step = step;

        // Pre-compile (parse + validate) each expression eagerly
        this.xCompiled = new CompiledFunction(xExpression);
        this.yCompiled = new CompiledFunction(yExpression);
        this.zCompiled = new CompiledFunction(zExpression);
    }

    /** Expression for the x-coordinate in terms of {@code t}. */
    public String xExpression() {
        return xExpression;
    }

    /** Expression for the y-coordinate in terms of {@code t}. */
    public String yExpression() {
        return yExpression;
    }

    /** Expression for the z-coordinate in terms of {@code t}. */
    public String zExpression() {
        return zExpression;
    }

    /** Lower bound of the parameter {@code t}. */
    public double start() {
        return start;
    }

    /** Upper bound of the parameter {@code t}. */
    public double end() {
        return end;
    }

    /** Sampling increment; must be positive. */
    public double step() {
        return step;
    }

    /**
     * Returns the pre-compiled x-expression for fast repeated evaluation.
     *
     * @return the compiled x function
     */
    public CompiledFunction xCompiled() {
        return xCompiled;
    }

    /**
     * Returns the pre-compiled y-expression for fast repeated evaluation.
     *
     * @return the compiled y function
     */
    public CompiledFunction yCompiled() {
        return yCompiled;
    }

    /**
     * Returns the pre-compiled z-expression for fast repeated evaluation.
     *
     * @return the compiled z function
     */
    public CompiledFunction zCompiled() {
        return zCompiled;
    }

    /**
     * Returns the set of user-controllable parameter names across all three
     * coordinate expressions.
     * <p>
     * These are variable names that are <em>not</em> system-reserved
     * ({@code t, x, y, z, PI, E}). For example, {@code "a*sin(b*t)"}
     * returns {@code {a, b}}.
     *
     * @return immutable set of parameter names (never null)
     */
    public Set<String> parameterNames() {
        ParameterExtractor extractor = new ParameterExtractor();
        Set<String> names = new LinkedHashSet<>();
        names.addAll(extractor.extract(xCompiled.ast()));
        names.addAll(extractor.extract(yCompiled.ast()));
        names.addAll(extractor.extract(zCompiled.ast()));
        return Set.copyOf(names);
    }

    /**
     * Returns the number of samples this definition would produce.
     * Computed as {@code max(0, floor((end - start) / step)) + 1}.
     */
    public long sampleCount() {
        if (end < start) return 0;
        return (long) (Math.floor((end - start) / step)) + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionDefinition that)) return false;
        return Double.compare(start, that.start) == 0
                && Double.compare(end, that.end) == 0
                && Double.compare(step, that.step) == 0
                && xExpression.equals(that.xExpression)
                && yExpression.equals(that.yExpression)
                && zExpression.equals(that.zExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xExpression, yExpression, zExpression, start, end, step);
    }

    @Override
    public String toString() {
        return "FunctionDefinition{"
                + "x='" + xExpression + '\''
                + ", y='" + yExpression + '\''
                + ", z='" + zExpression + '\''
                + ", start=" + start
                + ", end=" + end
                + ", step=" + step
                + '}';
    }
}
