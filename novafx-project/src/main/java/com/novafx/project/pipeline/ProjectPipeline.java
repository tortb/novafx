package com.novafx.project.pipeline;

import com.novafx.core.error.ErrorCode;
import com.novafx.core.error.ErrorCollector;
import com.novafx.core.error.ProjectError;
import com.novafx.math.AstFunctionSampler;
import com.novafx.math.FunctionDefinition;
import com.novafx.math.FunctionSampler;
import com.novafx.math.Vector3d;
import com.novafx.project.io.NfxcWriter;
import com.novafx.project.model.CompiledPointCloud;
import com.novafx.project.model.Project;
import com.novafx.project.migration.VersionMigrator;
import com.novafx.project.validation.NfxValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * Unified pipeline orchestrator for NovaFX project compilation.
 * <p>
 * Stages:
 * <ol>
 *   <li><b>Migrate</b> — forward-migrate file format version</li>
 *   <li><b>Validate</b> — structural and semantic validation</li>
 *   <li><b>Sample</b> — evaluate the parametric function to produce points</li>
 *   <li><b>Write</b> — write the binary {@code .nfxc} cache file</li>
 * </ol>
 * <p>
 * The pipeline collects all errors and warnings into an {@link ErrorCollector}.
 * Stages 2–4 are skipped when the collector already contains errors.
 */
public final class ProjectPipeline {

    private static final Logger log = LoggerFactory.getLogger(ProjectPipeline.class);

    private final NfxValidator validator;
    private final VersionMigrator migrator;
    private final FunctionSampler sampler;
    private final NfxcWriter nfxcWriter;

    /** Creates a pipeline with default components. */
    public ProjectPipeline() {
        this.validator = new NfxValidator();
        this.migrator = new VersionMigrator();
        this.sampler = new AstFunctionSampler();
        this.nfxcWriter = new NfxcWriter();
    }

    /**
     * Creates a pipeline with custom components (useful for testing).
     */
    ProjectPipeline(NfxValidator validator, VersionMigrator migrator,
                    FunctionSampler sampler, NfxcWriter nfxcWriter) {
        this.validator = validator;
        this.migrator = migrator;
        this.sampler = sampler;
        this.nfxcWriter = nfxcWriter;
    }

    // ── Execution ──

    /**
     * Executes the full pipeline: migrate → validate → sample → write cache.
     *
     * @param project    the file-format project to process
     * @param outputPath the target path for the {@code .nfxc} binary
     * @return a {@link PipelineResult} with points, diagnostics, and status
     */
    public PipelineResult execute(Project project, Path outputPath) {
        var collector = new ErrorCollector();
        log.debug("Pipeline started for '{}'", project.meta().name());

        // Stage 1: Migrate
        Project migrated = migrator.ensureCurrent(project, collector);
        if (collector.hasErrors()) {
            log.warn("Pipeline aborted after migration — {} error(s)", collector.size());
            return PipelineResult.failure(collector, outputPath);
        }

        // Stage 2: Validate
        validator.validate(migrated, collector);
        if (collector.hasErrors()) {
            log.warn("Pipeline aborted after validation — {} error(s)", collector.size());
            return PipelineResult.failure(collector, outputPath);
        }

        // Stage 3: Sample
        FunctionDefinition function = migrated.function();
        List<Vector3d> points;
        try {
            points = sampler.sample(function);
            log.debug("Sampled {} points", points.size());
        } catch (Exception e) {
            collector.addError(ErrorCode.COMPILATION_FAILED,
                    "Function sampling failed: " + e.getMessage(), "sampling");
            return PipelineResult.failure(collector, outputPath);
        }

        if (points.isEmpty()) {
            collector.addWarning(ErrorCode.EMPTY_POINT_CLOUD,
                    "Function produced zero points — check the parameter range",
                    "sampling");
        }

        // Stage 4: Write nfxc binary cache
        if (outputPath != null) {
            try {
                CompiledPointCloud cloud = CompiledPointCloud.from(points);
                nfxcWriter.write(cloud, outputPath);
                log.debug("Wrote nfxc cache to {}", outputPath);
            } catch (Exception e) {
                collector.addWarning(ErrorCode.CACHE_WRITE_FAILED,
                        "Failed to write nfxc cache: " + e.getMessage(), "output");
            }
        }

        log.info("Pipeline completed: {} points, {} diagnostic(s)",
                points.size(), collector.size());
        return PipelineResult.success(points, outputPath, collector);
    }

    /**
     * Convenience method: validate only, without compiling or writing output.
     */
    public ErrorCollector validateOnly(Project project) {
        var collector = new ErrorCollector();
        Project migrated = migrator.ensureCurrent(project, collector);
        if (!collector.hasErrors()) {
            validator.validate(migrated, collector);
        }
        return collector;
    }

    /**
     * Shortcut: migrate and validate, but don't compile.
     */
    public ErrorCollector validateAfterMigration(Project project) {
        var collector = new ErrorCollector();
        Project migrated = migrator.ensureCurrent(project, collector);
        if (!collector.hasErrors()) {
            validator.validate(migrated, collector);
        }
        return collector;
    }
}
