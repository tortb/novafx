package com.novafx.function;

/**
 * Categorizes a {@link CompletionItem} by its semantic type.
 */
public enum CompletionKind {
    /** Built-in mathematical function (sin, cos, sqrt, …). */
    FUNCTION,
    /** Variable reference (t, x, y, z). */
    VARIABLE,
    /** Named constant (PI, E). */
    CONSTANT,
    /** Expression snippet template (Spiral, Heart, …). */
    SNIPPET
}
