package com.novafx.export;

import com.novafx.core.domain.Project;
import com.novafx.math.FunctionDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class NfxzExporterTest {

    @TempDir
    Path tempDir;

    private static final FunctionDefinition DEF = new FunctionDefinition(
            "cos(t)", "sin(t)", "t/10", 0, 6.283, 0.05
    );

    @Test
    void shouldExportAndImport() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Project original = new Project(id, "TestProject", "A test", DEF, now, now);

        Path nfxz = tempDir.resolve("test.nfxz");
        NfxzExporter.export(original, nfxz);

        assertThat(nfxz).exists().isRegularFile();
        assertThat(nfxz.toFile().length()).isPositive();

        Project imported = NfxzExporter.importProject(nfxz);
        assertThat(imported.id()).isEqualTo(id);
        assertThat(imported.name()).isEqualTo("TestProject");
        assertThat(imported.description()).isEqualTo("A test");
        assertThat(imported.functionDefinition()).isEqualTo(DEF);
        assertThat(imported.createdAt()).isEqualTo(now);
    }

    @Test
    void shouldExportAndImportWithoutDescription() {
        Project p = new Project(UUID.randomUUID(), "NoDesc", null, DEF, Instant.now(), Instant.now());

        Path nfxz = tempDir.resolve("nodesc.nfxz");
        NfxzExporter.export(p, nfxz);

        Project imported = NfxzExporter.importProject(nfxz);
        assertThat(imported.name()).isEqualTo("NoDesc");
        assertThat(imported.description()).isEmpty();
    }

    @Test
    void shouldHandleComplexFunctionDefinition() {
        FunctionDefinition complex = new FunctionDefinition(
                "16*pow(sin(t),3)",
                "13*cos(t)-5*cos(2*t)-2*cos(3*t)-cos(4*t)",
                "0",
                0, 2 * Math.PI, 0.05
        );
        Project p = new Project(UUID.randomUUID(), "Heart", "", complex, Instant.now(), Instant.now());

        Path nfxz = tempDir.resolve("heart.nfxz");
        NfxzExporter.export(p, nfxz);

        Project imported = NfxzExporter.importProject(nfxz);
        assertThat(imported.functionDefinition()).isEqualTo(complex);
    }

    @Test
    void shouldRejectInvalidFile() {
        Path empty = tempDir.resolve("empty.nfxz");
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> NfxzExporter.importProject(empty));
    }

    @Test
    void exportedFileShouldBeZipFormat() throws Exception {
        Project p = new Project(UUID.randomUUID(), "Test", "", DEF, Instant.now(), Instant.now());
        Path nfxz = tempDir.resolve("test.nfxz");
        NfxzExporter.export(p, nfxz);

        // Verify it's a valid ZIP by checking magic bytes
        byte[] magic = java.nio.file.Files.readAllBytes(nfxz);
        assertThat(magic[0]).isEqualTo((byte) 0x50); // P
        assertThat(magic[1]).isEqualTo((byte) 0x4b); // K
    }
}
