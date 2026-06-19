package com.novafx.project.validation;

import com.novafx.core.error.ErrorCode;
import com.novafx.core.error.ErrorCollector;
import com.novafx.function.AstNode;
import com.novafx.function.AstParser;
import com.novafx.function.Tokenizer;
import com.novafx.project.model.Project;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Structural and semantic validator for NovaFX project files ({@code .nfx}).
 * <p>
 * Validates every section of the file-format {@link Project} model and
 * reports findings (errors, warnings, info) to an {@link ErrorCollector}.
 * The collector can then be interrogated by the pipeline or UI layer.
 * <p>
 * Validation <em>never</em> throws — all diagnostics go through the collector.
 */
public final class NfxValidator {

    private static final Pattern VALID_NAME_PATTERN =
            Pattern.compile("^[\\w\\s\\-.'一-鿿]+$");

    private static final Pattern VALID_PARAMETER_NAME =
            Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    /** Maximum allowed sample count (prevents OOM). */
    static final long MAX_SAMPLE_COUNT = 100_000_000;

    // ── Entry point ──

    /**
     * Validates a fully-parsed project.  All findings are written to
     * {@code collector}.  Call {@link ErrorCollector#hasErrors()} after
     * this method to decide whether to abort the pipeline.
     *
     * @param project  the file-format project (must not be null)
     * @param collector error collector (must not be null)
     */
    public void validate(Project project, ErrorCollector collector) {
        validateVersion(project, collector);
        validateMeta(project, collector);
        validateFunction(project, collector);
        validateParameters(project, collector);
        validateParticle(project, collector);
        validateRender(project, collector);
    }

    // ── Version ──

    private void validateVersion(Project project, ErrorCollector collector) {
        String v = project.version();
        if (v == null || v.isBlank()) {
            collector.addError(ErrorCode.MISSING_VERSION,
                    "Project file is missing a version field");
            return;
        }
        if (!Project.CURRENT_VERSION.equals(v)) {
            collector.addWarning(ErrorCode.UNSUPPORTED_VERSION,
                    "Project version '" + v + "' differs from current '"
                            + Project.CURRENT_VERSION + "' — will attempt migration");
        }
    }

    // ── Meta ──

    private void validateMeta(Project project, ErrorCollector collector) {
        if (project.meta() == null) {
            collector.addWarning(ErrorCode.MISSING_SECTION,
                    "Missing [meta] section — using defaults", "meta");
            return;
        }
        String name = project.meta().name();
        if (name == null || name.isBlank()) {
            collector.addWarning(ErrorCode.EMPTY_PROJECT_NAME,
                    "Project name is empty — will use 'Untitled'", "meta.name");
        } else if (!VALID_NAME_PATTERN.matcher(name).matches()) {
            collector.addWarning(ErrorCode.INVALID_PROJECT_NAME,
                    "Project name contains unusual characters: '" + name + "'", "meta.name");
        }
    }

    // ── Function ──

    private void validateFunction(Project project, ErrorCollector collector) {
        var func = project.function();
        if (func == null) {
            collector.addError(ErrorCode.MISSING_SECTION,
                    "Missing [function] section — a parametric function is required", "function");
            return;
        }

        // ── Validate each expression ──
        validateExpression("function.x", func.xExpression(), collector);
        validateExpression("function.y", func.yExpression(), collector);
        validateExpression("function.z", func.zExpression(), collector);

        // ── Validate range ──
        double start = func.start();
        double end = func.end();
        double step = func.step();

        if (Double.isNaN(start) || Double.isInfinite(start)) {
            collector.addError(ErrorCode.INVALID_RANGE,
                    "Start value is NaN or infinite", "function.start");
        }
        if (Double.isNaN(end) || Double.isInfinite(end)) {
            collector.addError(ErrorCode.INVALID_RANGE,
                    "End value is NaN or infinite", "function.end");
        }
        if (Double.isNaN(step) || Double.isInfinite(step)) {
            collector.addError(ErrorCode.INVALID_RANGE,
                    "Step value is NaN or infinite", "function.step");
        }
        if (step <= 0) {
            collector.addError(ErrorCode.INVALID_RANGE,
                    "Step must be positive (got " + step + ")", "function.step");
        }

        // Only validate ordering / count if nothing obviously wrong with range
        if (!collector.hasErrors() && end > start && step > 0) {
            long count = (long) Math.ceil((end - start) / step);
            if (count > MAX_SAMPLE_COUNT) {
                collector.addWarning(ErrorCode.SAMPLE_COUNT_EXCEEDED,
                        "Sample count " + count + " exceeds recommended maximum of "
                                + MAX_SAMPLE_COUNT + " — performance may degrade",
                        "function");
            }
            if (count == 0) {
                collector.addWarning(ErrorCode.EMPTY_POINT_CLOUD,
                        "Range produces zero sample points (end <= start)", "function");
            }
        }
    }

    // ── Expression validation ──

    private void validateExpression(String field, String expr, ErrorCollector collector) {
        if (expr == null || expr.isBlank()) {
            collector.addError(ErrorCode.MISSING_FIELD,
                    "Expression is empty", field);
            return;
        }
        try {
            var tokenizer = new Tokenizer(expr);
            var parser = new AstParser(tokenizer);
            AstNode ast = parser.parse();
            if (ast == null) {
                collector.addError(ErrorCode.EXPRESSION_PARSE_FAILED,
                        "Expression produced a null AST: '" + expr + "'", field);
            }
        } catch (Exception e) {
            collector.addError(ErrorCode.EXPRESSION_PARSE_FAILED,
                    "Failed to parse expression '" + expr + "': " + e.getMessage(), field);
        }
    }

    // ── Parameters ──

    private void validateParameters(Project project, ErrorCollector collector) {
        var params = project.parameters();
        if (params == null || params.isEmpty()) return;

        Set<String> seen = new HashSet<>();
        for (var entry : params.entrySet()) {
            String name = entry.getKey();
            Double value = entry.getValue();

            if (name == null || name.isBlank()) {
                collector.addWarning(ErrorCode.INVALID_IDENTIFIER,
                        "Parameter has blank name — skipping", "parameter");
                continue;
            }
            if (!VALID_PARAMETER_NAME.matcher(name).matches()) {
                collector.addWarning(ErrorCode.INVALID_IDENTIFIER,
                        "Parameter name '" + name + "' is not a valid identifier", "parameter." + name);
            }
            if (!seen.add(name)) {
                collector.addWarning(ErrorCode.DUPLICATE_FIELD,
                        "Duplicate parameter '" + name + "' — keeping first", "parameter." + name);
            }
            if (value == null || Double.isNaN(value) || Double.isInfinite(value)) {
                collector.addWarning(ErrorCode.MISSING_FIELD,
                        "Parameter '" + name + "' has invalid value", "parameter." + name);
            }
        }
    }

    // ── Particle ──

    private void validateParticle(Project project, ErrorCollector collector) {
        var p = project.particle();
        if (p == null) {
            collector.addInfo(ErrorCode.MISSING_SECTION,
                    "No [particle] section — using defaults", "particle");
            return;
        }
        if (p.size() <= 0) {
            collector.addWarning(ErrorCode.INVALID_RANGE,
                    "Particle size must be positive (got " + p.size() + ")", "particle.size");
        }
        if (p.density() <= 0) {
            collector.addWarning(ErrorCode.INVALID_RANGE,
                    "Particle density must be positive (got " + p.density() + ")", "particle.density");
        }
    }

    // ── Render ──

    private void validateRender(Project project, ErrorCollector collector) {
        var r = project.render();
        if (r == null) {
            collector.addInfo(ErrorCode.MISSING_SECTION,
                    "No [render] section — using defaults", "render");
        }
    }
}
