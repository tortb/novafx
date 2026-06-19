package com.novafx.project;

import com.novafx.core.domain.Project;
import com.novafx.core.domain.PlatformService;
import com.novafx.math.FunctionDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JacksonProjectRepositoryTest {

    private JacksonProjectRepository repository;

    @TempDir
    Path tempDir;

    private static final FunctionDefinition DEF = new FunctionDefinition("cos(t)", "sin(t)", "0", 0, 1, 0.1);

    @BeforeEach
    void setUp() {
        repository = new JacksonProjectRepository(tempDir);
    }

    @Test
    void shouldSaveAndLoadProject() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Project original = new Project(id, "Test", "desc", DEF, now, now);

        repository.save(original);

        Optional<Project> loaded = repository.findById(id);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().id()).isEqualTo(id);
        assertThat(loaded.get().name()).isEqualTo("Test");
        assertThat(loaded.get().description()).isEqualTo("desc");
        assertThat(loaded.get().functionDefinition()).isEqualTo(DEF);
        assertThat(loaded.get().createdAt()).isEqualTo(now);
    }

    @Test
    void shouldReturnEmptyForMissingProject() {
        Optional<Project> result = repository.findById(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldListAllProjects() {
        Project a = new Project(UUID.randomUUID(), "A", "", DEF, Instant.now(), Instant.now());
        Project b = new Project(UUID.randomUUID(), "B", "", DEF, Instant.now(), Instant.now());

        repository.save(a);
        repository.save(b);

        List<Project> all = repository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(Project::id).containsExactlyInAnyOrder(a.id(), b.id());
    }

    @Test
    void shouldReturnEmptyListWhenNoProjects() {
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldDeleteProject() {
        UUID id = UUID.randomUUID();
        Project p = new Project(id, "ToDelete", "", DEF, Instant.now(), Instant.now());
        repository.save(p);

        boolean deleted = repository.deleteById(id);
        assertThat(deleted).isTrue();
        assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistent() {
        boolean deleted = repository.deleteById(UUID.randomUUID());
        assertThat(deleted).isFalse();
    }

    @Test
    void shouldCountProjects() {
        assertThat(repository.count()).isEqualTo(0);

        repository.save(new Project(UUID.randomUUID(), "A", "", DEF, Instant.now(), Instant.now()));
        assertThat(repository.count()).isEqualTo(1);

        repository.save(new Project(UUID.randomUUID(), "B", "", DEF, Instant.now(), Instant.now()));
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void shouldOverwriteExistingProject() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Project original = new Project(id, "Original", "", DEF, now, now);
        repository.save(original);

        FunctionDefinition def2 = new FunctionDefinition("t", "t", "t", 0, 2, 0.5);
        Project updated = new Project(id, "Updated", "changed", def2, now, now);
        repository.save(updated);

        Optional<Project> loaded = repository.findById(id);
        assertThat(loaded).isPresent();
        assertThat(loaded.get().name()).isEqualTo("Updated");
        assertThat(loaded.get().functionDefinition()).isEqualTo(def2);
    }

    @Test
    void shouldPersistComplexFunctionDefinition() {
        FunctionDefinition complex = new FunctionDefinition(
                "16*pow(sin(t),3)",
                "13*cos(t)-5*cos(2*t)-2*cos(3*t)-cos(4*t)",
                "0",
                0, 2 * Math.PI, 0.05
        );
        UUID id = UUID.randomUUID();
        Project p = new Project(id, "Heart", "heart preset", complex, Instant.now(), Instant.now());
        repository.save(p);

        Project loaded = repository.findById(id).orElseThrow();
        assertThat(loaded.functionDefinition()).isEqualTo(complex);
    }

    @Test
    void shouldHandleSaveAndListOrderIndependence() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        repository.save(new Project(id2, "B", "", DEF, Instant.now(), Instant.now()));
        repository.save(new Project(id1, "A", "", DEF, Instant.now(), Instant.now()));

        assertThat(repository.findAll()).hasSize(2);
        assertThat(repository.findById(id1)).isPresent();
        assertThat(repository.findById(id2)).isPresent();
    }
}
