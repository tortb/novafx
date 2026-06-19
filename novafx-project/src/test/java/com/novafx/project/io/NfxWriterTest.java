package com.novafx.project.io;

import com.novafx.math.FunctionDefinition;
import com.novafx.project.model.Meta;
import com.novafx.project.model.ParticleSettings;
import com.novafx.project.model.Project;
import com.novafx.project.model.RenderSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link NfxWriter} TOML serialization.
 */
class NfxWriterTest {

    private final NfxWriter writer = new NfxWriter();

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteMinimalProject() throws IOException {
        var project = Project.from(new FunctionDefinition("t", "t", "0", 0, 10, 0.5));
        Path file = tempDir.resolve("minimal.nfx");

        writer.write(project, file);

        assertThat(file).isRegularFile();
        String content = Files.readString(file);
        assertThat(content).contains("version = \"1.0\"");
        assertThat(content).contains("[meta]");
        assertThat(content).contains("[function]");
        assertThat(content).contains("[particle]");
        assertThat(content).contains("[render]");
    }

    @Test
    void shouldWriteProjectWithAllFields() throws IOException {
        var func = new FunctionDefinition("cos(t)", "sin(t)", "t", 0, 6.28, 0.01);
        var project = new Project("1.0",
                new Meta("DNA", "tortb"),
                func,
                new ParticleSettings(0.5, 2.0),
                new RenderSettings(true, false));
        Path file = tempDir.resolve("dna.nfx");

        writer.write(project, file);

        String content = Files.readString(file);
        assertThat(content).contains("name = \"DNA\"");
        assertThat(content).contains("author = \"tortb\"");
        assertThat(content).contains("x = \"cos(t)\"");
        assertThat(content).contains("y = \"sin(t)\"");
        assertThat(content).contains("z = \"t\"");
        assertThat(content).contains("size = 0.5");
        assertThat(content).contains("density = 2");
        assertThat(content).contains("grid = true");
        assertThat(content).contains("axis = false");
    }

    @Test
    void shouldWriteChineseCharacters() throws IOException {
        var func = new FunctionDefinition("t", "t", "0", 0, 1, 0.1);
        var project = new Project("1.0",
                new Meta("心形线", "张三"),
                func,
                ParticleSettings.defaults(),
                RenderSettings.defaults());
        Path file = tempDir.resolve("chinese.nfx");

        writer.write(project, file);

        String content = Files.readString(file);
        assertThat(content).contains("心形线");
        assertThat(content).contains("张三");
    }

    @Test
    void shouldWriteExpressionWithSpecialCharacters() throws IOException {
        var func = new FunctionDefinition("16*pow(sin(t),3)", "13*cos(t)-5*cos(2*t)", "0", 0, 6.28, 0.05);
        var project = Project.from(func);
        Path file = tempDir.resolve("special.nfx");

        writer.write(project, file);

        String content = Files.readString(file);
        assertThat(content).contains("16*pow(sin(t),3)");
        assertThat(content).contains("13*cos(t)-5*cos(2*t)");
    }
}
