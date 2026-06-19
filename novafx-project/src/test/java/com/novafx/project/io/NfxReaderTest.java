package com.novafx.project.io;

import com.novafx.math.FunctionDefinition;
import com.novafx.project.ProjectFormatException;
import com.novafx.project.model.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link NfxReader} TOML parsing.
 */
class NfxReaderTest {

    private final NfxReader reader = new NfxReader();

    @TempDir
    Path tempDir;

    @Test
    void shouldReadFullProject() throws IOException {
        String toml = """
                version = "1.0"

                [meta]
                name = "DNA"
                author = "tortb"

                [function]
                x = "cos(t)"
                y = "sin(t)"
                z = "t"
                start = 0
                end = 6.28
                step = 0.01

                [particle]
                size = 0.5
                density = 2.0

                [render]
                grid = true
                axis = false
                """;
        Path file = tempDir.resolve("test.nfx");
        Files.writeString(file, toml);

        Project project = reader.read(file);

        assertThat(project.version()).isEqualTo("1.0");
        assertThat(project.meta().name()).isEqualTo("DNA");
        assertThat(project.meta().author()).isEqualTo("tortb");
        assertThat(project.function().xExpression()).isEqualTo("cos(t)");
        assertThat(project.function().yExpression()).isEqualTo("sin(t)");
        assertThat(project.function().zExpression()).isEqualTo("t");
        assertThat(project.function().start()).isEqualTo(0.0);
        assertThat(project.function().end()).isEqualTo(6.28);
        assertThat(project.function().step()).isEqualTo(0.01);
        assertThat(project.particle().size()).isEqualTo(0.5);
        assertThat(project.particle().density()).isEqualTo(2.0);
        assertThat(project.render().grid()).isTrue();
        assertThat(project.render().axis()).isFalse();
    }

    @Test
    void shouldReadMinimalProject() throws IOException {
        String toml = """
                version = "1.0"

                [function]
                x = "t"
                y = "t"
                z = "0"
                start = 0
                end = 10
                step = 0.1
                """;
        Path file = tempDir.resolve("minimal.nfx");
        Files.writeString(file, toml);

        Project project = reader.read(file);

        assertThat(project.version()).isEqualTo("1.0");
        assertThat(project.meta().name()).isEqualTo("Untitled");
        assertThat(project.function()).isNotNull();
        assertThat(project.particle()).isNotNull();
        assertThat(project.render()).isNotNull();
    }

    @Test
    void shouldReadChineseCharacters() throws IOException {
        String toml = """
                version = "1.0"

                [meta]
                name = "心形线"
                author = "张三"

                [function]
                x = "16*pow(sin(t),3)"
                y = "13*cos(t)-5*cos(2*t)-2*cos(3*t)-cos(4*t)"
                z = "0"
                start = 0
                end = 6.2832
                step = 0.05
                """;
        Path file = tempDir.resolve("heart.nfx");
        Files.writeString(file, toml);

        Project project = reader.read(file);

        assertThat(project.meta().name()).isEqualTo("心形线");
        assertThat(project.meta().author()).isEqualTo("张三");
    }

    @Test
    void shouldThrowOnMissingVersion() throws IOException {
        String toml = """
                [function]
                x = "t"
                y = "t"
                z = "0"
                """;
        Path file = tempDir.resolve("noversion.nfx");
        Files.writeString(file, toml);

        assertThatThrownBy(() -> reader.read(file))
                .isInstanceOf(ProjectFormatException.class)
                .hasMessageContaining("version");
    }

    @Test
    void shouldThrowOnUnknownVersion() throws IOException {
        String toml = """
                version = "99.0"

                [function]
                x = "t"
                y = "t"
                z = "0"
                """;
        Path file = tempDir.resolve("badver.nfx");
        Files.writeString(file, toml);

        assertThatThrownBy(() -> reader.read(file))
                .isInstanceOf(ProjectFormatException.class)
                .hasMessageContaining("version");
    }

    @Test
    void shouldThrowOnIllegalToml() throws IOException {
        String badToml = "version = \"1.0\"\n[function\nx = \"t\"\n"; // malformed table
        Path file = tempDir.resolve("bad.nfx");
        Files.writeString(file, badToml);

        assertThatThrownBy(() -> reader.read(file))
                .isInstanceOf(ProjectFormatException.class)
                .hasMessageContaining("TOML");
    }

    @Test
    void shouldThrowOnMissingFunction() throws IOException {
        String toml = """
                version = "1.0"

                [meta]
                name = "test"
                """;
        Path file = tempDir.resolve("nofunc.nfx");
        Files.writeString(file, toml);

        assertThatThrownBy(() -> reader.read(file))
                .isInstanceOf(ProjectFormatException.class)
                .hasMessageContaining("function");
    }

    @Test
    void shouldThrowOnEmptyFile() throws IOException {
        Path file = tempDir.resolve("empty.nfx");
        Files.writeString(file, "");

        assertThatThrownBy(() -> reader.read(file))
                .isInstanceOf(ProjectFormatException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void shouldRoundTripWithWriter() {
        var original = new Project("1.0",
                new com.novafx.project.model.Meta("RoundTrip", "tester"),
                new FunctionDefinition("cos(t)", "sin(t)", "t", 0, 6.28, 0.01),
                new com.novafx.project.model.ParticleSettings(0.3, 1.5),
                new com.novafx.project.model.RenderSettings(true, true));
        Path file = tempDir.resolve("roundtrip.nfx");

        new NfxWriter().write(original, file);
        Project loaded = reader.read(file);

        assertThat(loaded.version()).isEqualTo(original.version());
        assertThat(loaded.meta().name()).isEqualTo(original.meta().name());
        assertThat(loaded.meta().author()).isEqualTo(original.meta().author());
        assertThat(loaded.function().xExpression()).isEqualTo(original.function().xExpression());
        assertThat(loaded.particle().size()).isEqualTo(original.particle().size());
        assertThat(loaded.render().grid()).isEqualTo(original.render().grid());
    }

    @Test
    void shouldReadUtf8Bom() throws IOException {
        // TOML allows (and ignores) a UTF-8 BOM
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String toml = "version = \"1.0\"\n\n[function]\nx = \"t\"\ny = \"t\"\nz = \"0\"\n";
        byte[] content = new byte[bom.length + toml.getBytes(StandardCharsets.UTF_8).length];
        System.arraycopy(bom, 0, content, 0, bom.length);
        System.arraycopy(toml.getBytes(StandardCharsets.UTF_8), 0, content, bom.length, toml.getBytes(StandardCharsets.UTF_8).length);
        Path file = tempDir.resolve("bom.nfx");
        Files.write(file, content);

        // Should not throw
        Project project = reader.read(file);
        assertThat(project).isNotNull();
    }
}
