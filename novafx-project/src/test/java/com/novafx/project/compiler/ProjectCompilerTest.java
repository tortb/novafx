package com.novafx.project.compiler;

import com.novafx.math.FunctionDefinition;
import com.novafx.project.io.NfxcReader;
import com.novafx.project.model.CompiledPointCloud;
import com.novafx.project.model.Meta;
import com.novafx.project.model.ParticleSettings;
import com.novafx.project.model.Project;
import com.novafx.project.model.RenderSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ProjectCompiler}.
 */
class ProjectCompilerTest {

    private final ProjectCompiler compiler = new ProjectCompiler();
    private final NfxcReader nfxcReader = new NfxcReader();

    @TempDir
    Path tempDir;

    @Test
    void shouldCompileSimpleProject() {
        var func = new FunctionDefinition("cos(t)", "sin(t)", "t", 0, 6.2832, 0.1);
        var project = new Project("1.0",
                new Meta("Circle", "test"),
                func,
                ParticleSettings.defaults(),
                RenderSettings.defaults());
        Path output = tempDir.resolve("circle.nfxc");

        compiler.compile(project, output);

        assertThat(output).isRegularFile();
        CompiledPointCloud cloud = nfxcReader.read(output);
        assertThat(cloud.pointCount()).isPositive();
        assertThat(cloud.points()).isNotEmpty();
    }

    @Test
    void shouldProduceCorrectPointCount() {
        var func = new FunctionDefinition("t", "t", "0", 0, 10, 0.5);
        var project = Project.from(func);
        long expectedCount = func.sampleCount();
        Path output = tempDir.resolve("line.nfxc");

        compiler.compile(project, output);

        CompiledPointCloud cloud = nfxcReader.read(output);
        assertThat(cloud.pointCount()).isEqualTo((int) expectedCount);
    }

    @Test
    void shouldComputeBoundingBox() {
        var func = new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 6.2832, 0.1);
        var project = Project.from(func);
        Path output = tempDir.resolve("bbox.nfxc");

        compiler.compile(project, output);

        CompiledPointCloud cloud = nfxcReader.read(output);
        assertThat(cloud.minX()).isLessThanOrEqualTo(cloud.maxX());
        assertThat(cloud.minY()).isLessThanOrEqualTo(cloud.maxY());
        assertThat(cloud.minZ()).isLessThanOrEqualTo(cloud.maxZ());
    }

    @Test
    void shouldCompileOneMillionPoints() {
        // ~1M points: (10 - 0) / 0.00001 = 1,000,001
        var func = new FunctionDefinition("cos(2*PI*t)", "sin(2*PI*t)", "t", 0, 10, 0.00001);
        var project = Project.from(func);
        Path output = tempDir.resolve("million-compile.nfxc");

        compiler.compile(project, output);

        CompiledPointCloud cloud = nfxcReader.read(output);
        assertThat(cloud.pointCount()).isGreaterThan(900_000);
        assertThat(output.toFile().length()).isGreaterThan(10_000_000L);
    }

    @Test
    void shouldCompileComplexFunction() {
        var func = new FunctionDefinition(
                "16*pow(sin(t),3)",
                "13*cos(t)-5*cos(2*t)-2*cos(3*t)-cos(4*t)",
                "0",
                0, 2 * Math.PI, 0.05
        );
        var project = Project.from(func);
        Path output = tempDir.resolve("heart.nfxc");

        compiler.compile(project, output);

        CompiledPointCloud cloud = nfxcReader.read(output);
        assertThat(cloud.pointCount()).isPositive();
        // Heart points should be within [-20, 20] range
        assertThat(cloud.maxX()).isLessThanOrEqualTo(20f);
        assertThat(cloud.maxY()).isLessThanOrEqualTo(20f);
    }
}
