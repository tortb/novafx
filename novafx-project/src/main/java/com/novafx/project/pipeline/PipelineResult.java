package com.novafx.project.pipeline;

import com.novafx.core.error.ErrorCollector;
import com.novafx.math.Vector3d;

import java.nio.file.Path;
import java.util.List;

/**
 * The result of a single {@link ProjectPipeline} run.
 * <p>
 * Contains the compiled point cloud, any errors or warnings, and
 * the output path.  Callers should check {@link #success()} before
 * using the points.
 */
public final class PipelineResult {

    private final List<Vector3d> points;
    private final ErrorCollector errors;
    private final Path outputPath;
    private final boolean success;

    private PipelineResult(List<Vector3d> points, ErrorCollector errors,
                           Path outputPath, boolean success) {
        this.points = points;
        this.errors = errors;
        this.outputPath = outputPath;
        this.success = success;
    }

    // ── Factory methods ──

    /**
     * Creates a successful result with compiled points.
     */
    public static PipelineResult success(List<Vector3d> points, Path outputPath,
                                          ErrorCollector errors) {
        return new PipelineResult(
                List.copyOf(points),
                errors,
                outputPath,
                true
        );
    }

    /**
     * Creates a failed result (no points).
     */
    public static PipelineResult failure(ErrorCollector errors) {
        return new PipelineResult(List.of(), errors, null, false);
    }

    public static PipelineResult failure(ErrorCollector errors, Path outputPath) {
        return new PipelineResult(List.of(), errors, outputPath, false);
    }

    // ── Accessors ──

    /**
     * Returns {@code true} when the pipeline completed without errors
     * and a valid point cloud was produced.
     */
    public boolean success() {
        return success;
    }

    /**
     * Returns the compiled point cloud (empty list on failure).
     */
    public List<Vector3d> points() {
        return points;
    }

    /**
     * Returns the path the compiled output was (or would have been) written to.
     */
    public Path outputPath() {
        return outputPath;
    }

    /**
     * Returns the error collector with all errors, warnings, and info messages.
     */
    public ErrorCollector errors() {
        return errors;
    }

    /**
     * Returns the number of compiled points.
     */
    public int pointCount() {
        return points.size();
    }

    /**
     * Returns a human-readable summary string.
     */
    public String summary() {
        if (success) {
            return "Compiled " + points.size() + " points with "
                    + errors.size() + " diagnostic(s)";
        }
        return "Failed with " + errors.size() + " error(s)";
    }
}
