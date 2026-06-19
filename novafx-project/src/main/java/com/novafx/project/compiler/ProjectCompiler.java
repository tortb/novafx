package com.novafx.project.compiler;

import com.novafx.math.AstFunctionSampler;
import com.novafx.math.FunctionSampler;
import com.novafx.math.Vector3d;
import com.novafx.project.io.NfxcWriter;
import com.novafx.project.model.CompiledPointCloud;
import com.novafx.project.model.Project;

import java.nio.file.Path;
import java.util.List;

/**
 * Compiles a NovaFX project into a binary {@code .nfxc} file.
 * <p>
 * The compilation pipeline:
 * <ol>
 *   <li>Extract the parametric function definition from the project</li>
 *   <li>Sample the function over its parameter domain</li>
 *   <li>Compute the bounding box</li>
 *   <li>Write the interleaved float buffer to a little-endian binary file</li>
 * </ol>
 */
public final class ProjectCompiler {

    private final FunctionSampler sampler;
    private final NfxcWriter nfxcWriter;

    /** Creates a compiler with default sampler and writer. */
    public ProjectCompiler() {
        this.sampler = new AstFunctionSampler();
        this.nfxcWriter = new NfxcWriter();
    }

    /**
     * Creates a compiler with custom sampler and writer (useful for testing).
     */
    ProjectCompiler(FunctionSampler sampler, NfxcWriter nfxcWriter) {
        this.sampler = sampler;
        this.nfxcWriter = nfxcWriter;
    }

    /**
     * Compiles the given project and writes the result to the specified path.
     *
     * @param project the project to compile; must not be null
     * @param output  the target path for the {@code .nfxc} file; must not be null
     * @throws IllegalArgumentException if the project has no function definition
     * @throws com.novafx.project.ProjectFormatException if compilation fails
     */
    public void compile(Project project, Path output) {
        var function = project.function();

        // Sample the parametric function
        List<Vector3d> vecs = sampler.sample(function);

        // Build point cloud
        CompiledPointCloud cloud = CompiledPointCloud.from(vecs);

        // Write binary file
        nfxcWriter.write(cloud, output);
    }
}
