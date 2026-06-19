package com.novafx.math;

import com.novafx.function.CompiledFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Samples a {@link FunctionDefinition} over its parameter range using
 * the NovaFX AST engine.
 * <p>
 * Each coordinate expression is parsed exactly once (when the
 * {@link FunctionDefinition} is constructed). The subsequent
 * per-point evaluation walks the compiled AST without re-parsing.
 */
public final class AstFunctionSampler implements FunctionSampler {

    private static final Logger log = LoggerFactory.getLogger(AstFunctionSampler.class);

    @Override
    public List<Vector3d> sample(FunctionDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null");
        }

        long count = definition.sampleCount();
        if (count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Sample count " + count + " exceeds maximum array size");
        }

        CompiledFunction xFunc = definition.xCompiled();
        CompiledFunction yFunc = definition.yCompiled();
        CompiledFunction zFunc = definition.zCompiled();

        double start = definition.start();
        double end = definition.end();
        double step = definition.step();
        int maxIterations = (int) count;

        List<Vector3d> points = new ArrayList<>(maxIterations);

        for (int i = 0; i < maxIterations; i++) {
            double t = start + i * step;
            if (t > end + 1e-12) break;

            Map<String, Double> vars = Map.of("t", t);

            double x = xFunc.evaluate(vars);
            double y = yFunc.evaluate(vars);
            double z = zFunc.evaluate(vars);

            points.add(new Vector3d(x, y, z));
        }

        log.debug("Sampled {} points from {}", points.size(), definition);
        return points;
    }
}
