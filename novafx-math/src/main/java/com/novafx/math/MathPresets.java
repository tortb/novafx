package com.novafx.math;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Built-in parametric function presets.
 * <p>
 * Each preset has a human-readable name and a corresponding
 * {@link FunctionDefinition} that produces a well-known 3D shape.
 * All presets use the parameter variable {@code t}.
 */
public final class MathPresets {

    private MathPresets() {
        // utility class
    }

    /**
     * Returns an ordered map of all built-in presets keyed by name.
     * The order is stable and suitable for display in a UI list.
     *
     * @return name-to-definition map
     */
    public static Map<String, FunctionDefinition> all() {
        Map<String, FunctionDefinition> presets = new LinkedHashMap<>();
        presets.put("Circle", circle());
        presets.put("Heart", heart());
        presets.put("Star", star());
        presets.put("Spiral", spiral());
        presets.put("DoubleSpiral", doubleSpiral());
        presets.put("Infinity", infinity());
        presets.put("Flower", flower());
        presets.put("Wave", wave());
        presets.put("Helix", helix());
        return Collections.unmodifiableMap(new LinkedHashMap<>(presets));
    }

    /**
     * Returns a preset by name, or {@code null} if not found.
     *
     * @param name the preset name
     * @return the matching definition, or null
     */
    public static FunctionDefinition byName(String name) {
        return all().get(name);
    }

    /**
     * Returns the list of all preset names in display order.
     *
     * @return immutable list of names
     */
    public static List<String> names() {
        return List.copyOf(all().keySet());
    }

    // ---------------------------------------------------------------
    // Preset definitions
    // ---------------------------------------------------------------

    /** Circle in the XY-plane: radius 3, centered at origin. */
    public static FunctionDefinition circle() {
        return new FunctionDefinition(
                "3*cos(t)",
                "3*sin(t)",
                "0",
                0, 2 * Math.PI, 0.05
        );
    }

    /** Heart shape in the XY-plane using a classic parametric formula. */
    public static FunctionDefinition heart() {
        return new FunctionDefinition(
                "16*pow(sin(t),3)",
                "13*cos(t)-5*cos(2*t)-2*cos(3*t)-cos(4*t)",
                "0",
                0, 2 * Math.PI, 0.05
        );
    }

    /** 5-pointed star in the XY-plane. */
    public static FunctionDefinition star() {
        return new FunctionDefinition(
                "3*cos(1.25*t)*cos(t)",
                "3*cos(1.25*t)*sin(t)",
                "0",
                0, 8 * Math.PI, 0.05
        );
    }

    /** Flat spiral winding outward in the XY-plane. */
    public static FunctionDefinition spiral() {
        return new FunctionDefinition(
                "0.5*t*cos(t)",
                "0.5*t*sin(t)",
                "0",
                0, 8 * Math.PI, 0.05
        );
    }

    /** Two oppositely winding spirals. */
    public static FunctionDefinition doubleSpiral() {
        return new FunctionDefinition(
                "3*cos(t)",
                "3*sin(t)",
                "t/2",
                0, 8 * Math.PI, 0.05
        );
    }

    /** Infinity symbol (lemniscate) in the XY-plane. */
    public static FunctionDefinition infinity() {
        return new FunctionDefinition(
                "3*cos(t)/(1+pow(sin(t),2))",
                "3*cos(t)*sin(t)/(1+pow(sin(t),2))",
                "0",
                0, 2 * Math.PI, 0.03
        );
    }

    /** 5-petal flower (rose curve). */
    public static FunctionDefinition flower() {
        return new FunctionDefinition(
                "3*cos(2.5*t)*cos(t)",
                "3*cos(2.5*t)*sin(t)",
                "0",
                0, 4 * Math.PI, 0.03
        );
    }

    /** Sine wave in the XZ-plane. */
    public static FunctionDefinition wave() {
        return new FunctionDefinition(
                "t/2",
                "2*sin(t)",
                "0",
                0, 4 * Math.PI, 0.05
        );
    }

    /** 3D helix rising along the Z-axis. */
    public static FunctionDefinition helix() {
        return new FunctionDefinition(
                "3*cos(t)",
                "3*sin(t)",
                "t/3",
                0, 6 * Math.PI, 0.05
        );
    }
}
