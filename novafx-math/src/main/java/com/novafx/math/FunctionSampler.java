package com.novafx.math;

import java.util.List;

/**
 * Samples a parametric function definition over its parameter range
 * and produces a list of 3D points.
 */
@FunctionalInterface
public interface FunctionSampler {

    /**
     * Samples the given function definition across its domain
     * {@code [definition.start(), definition.end()]} with step
     * {@code definition.step()}.
     *
     * @param definition the parametric function to sample; must not be null
     * @return an ordered list of sampled 3D points; never null
     * @throws IllegalArgumentException if any expression in the definition
     *                                  cannot be evaluated
     */
    List<Vector3d> sample(FunctionDefinition definition);
}
