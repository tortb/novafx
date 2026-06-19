package com.novafx.core.workspace;

/**
 * Categorises every node in the project tree.
 * <p>
 * Each variant maps to a distinct kind of structural element that
 * appears in the {@link ProjectExplorer} tree. Container types
 * ({@link #PROJECT}, {@link #FUNCTION}, {@link #PARAMETER_LIST})
 * group related children; leaf types represent navigable or
 * editable elements.
 */
public enum ProjectNodeType {

    /** Root of a single .nfx project (container). */
    PROJECT,
    /** The parametric function section (container for X_EXPR, Y_EXPR, Z_EXPR). */
    FUNCTION,
    /** The x(t) coordinate expression (leaf). */
    X_EXPR,
    /** The y(t) coordinate expression (leaf). */
    Y_EXPR,
    /** The z(t) coordinate expression (leaf). */
    Z_EXPR,
    /** The parameter list section (container for individual PARAMETER nodes). */
    PARAMETER_LIST,
    /** A single user-defined parameter (leaf). */
    PARAMETER,
    /** Camera settings (leaf). */
    CAMERA,
    /** Render / display settings (leaf). */
    RENDER,
    /** Preset gallery (leaf). */
    PRESETS,

    // ── convenience grouping ──

    /** Sentinel: not a real tree node. */
    NONE;

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * Returns {@code true} when this type represents a container
     * that can be expanded to show children.
     */
    public boolean isContainer() {
        return switch (this) {
            case PROJECT, FUNCTION, PARAMETER_LIST -> true;
            default -> false;
        };
    }
}
