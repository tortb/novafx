package com.novafx.function;

/**
 * A named, adjustable parameter for a parametric expression.
 * <p>
 * Parameters are user-controllable variables automatically extracted
 * from expressions. For example, {@code "a * sin(b * t)"} yields
 * two parameters: {@code a} and {@code b}.
 * <p>
 * Each parameter has a current value and optional slider bounds.
 *
 * @param name  the variable name (e.g. "a", "b")
 * @param value the current numeric value
 * @param min   minimum slider value
 * @param max   maximum slider value
 * @param step  slider step increment
 */
public record Parameter(String name, double value, double min, double max, double step) {

    /** Default slider minimum. */
    public static final double DEFAULT_MIN = 0.0;

    /** Default slider maximum. */
    public static final double DEFAULT_MAX = 10.0;

    /** Default slider step. */
    public static final double DEFAULT_STEP = 0.1;

    /**
     * Creates a parameter with default slider bounds.
     *
     * @param name  the variable name
     * @param value the current numeric value
     */
    public Parameter(String name, double value) {
        this(name, value, DEFAULT_MIN, DEFAULT_MAX, DEFAULT_STEP);
    }

    /**
     * Returns a new parameter with the same metadata but a different value.
     *
     * @param newValue the new value
     * @return updated parameter
     */
    public Parameter withValue(double newValue) {
        return new Parameter(name, newValue, min, max, step);
    }
}
