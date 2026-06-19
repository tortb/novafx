package com.novafx.project.repository;

import com.novafx.core.domain.Project;
import com.novafx.math.FunctionDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ProjectRepositoryImpl}.
 */
class ProjectRepositoryImplTest {

    private ProjectRepositoryImpl repository;

    @TempDir
    Path tempDir;

    private static final FunctionDefinition DEF = new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 1, 0.1);

    @BeforeEach
    void setUp() {
        repository = new ProjectRepositoryImpl();
    }

    @Test
    void shouldSaveAndLoadProject() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Project original = new Project(id, "Test", "desc", DEF, now, now);
        Path file = tempDir.resolve("test.nfx");

        repository.save(original, file);

        Project loaded = repository.load(file);
        assertThat(loaded.name()).isEqualTo("Test");
        assertThat(loaded.functionDefinition()).isEqualTo(DEF);
    }

    @Test
    void shouldSaveChineseProject() {
        var def = new FunctionDefinition("16*pow(sin(t),3)", "13*cos(t)-5*cos(2*t)", "0", 0, 6.28, 0.05);
        Project original = new Project(UUID.randomUUID(), "心形线", "测试", def, Instant.now(), Instant.now());
        Path file = tempDir.resolve("chinese.nfx");

        repository.save(original, file);
        Project loaded = repository.load(file);

        assertThat(loaded.name()).isEqualTo("心形线");
        assertThat(loaded.functionDefinition()).isEqualTo(def);
    }

    @Test
    void shouldOverwriteExistingFile() {
        var def1 = new FunctionDefinition("t", "t", "0", 0, 1, 0.1);
        var def2 = new FunctionDefinition("t", "t", "t", 0, 2, 0.5);
        Path file = tempDir.resolve("overwrite.nfx");

        repository.save(new Project(UUID.randomUUID(), "First", "", def1, Instant.now(), Instant.now()), file);
        repository.save(new Project(UUID.randomUUID(), "Second", "", def2, Instant.now(), Instant.now()), file);

        Project loaded = repository.load(file);
        assertThat(loaded.name()).isEqualTo("Second");
        assertThat(loaded.functionDefinition()).isEqualTo(def2);
    }

    @Test
    void shouldPreserveFunctionExpressionAcrossSaveLoad() {
        var complex = new FunctionDefinition(
                "16*pow(sin(t),3)",
                "13*cos(t)-5*cos(2*t)-2*cos(3*t)-cos(4*t)",
                "0",
                0, 2 * Math.PI, 0.05
        );
        Project original = new Project(UUID.randomUUID(), "Heart", "heart preset", complex, Instant.now(), Instant.now());
        Path file = tempDir.resolve("heart.nfx");

        repository.save(original, file);
        Project loaded = repository.load(file);

        assertThat(loaded.functionDefinition()).isEqualTo(complex);
    }
}
