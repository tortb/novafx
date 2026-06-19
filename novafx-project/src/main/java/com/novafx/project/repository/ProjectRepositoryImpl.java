package com.novafx.project.repository;

import com.novafx.core.domain.Project;
import com.novafx.math.FunctionDefinition;
import com.novafx.project.io.NfxReader;
import com.novafx.project.io.NfxWriter;
import com.novafx.project.io.ProjectReader;
import com.novafx.project.io.ProjectWriter;
import com.novafx.project.model.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link ProjectRepository} backed by
 * {@link NfxReader} / {@link NfxWriter}.
 * <p>
 * Converts between the file-format model and the domain aggregate
 * at the repository boundary.
 */
public final class ProjectRepositoryImpl implements ProjectRepository {

    private static final Logger log = LoggerFactory.getLogger(ProjectRepositoryImpl.class);

    private final ProjectReader reader;
    private final ProjectWriter writer;

    /** Creates a repository with default reader and writer. */
    public ProjectRepositoryImpl() {
        this.reader = new NfxReader();
        this.writer = new NfxWriter();
    }

    /**
     * Creates a repository with custom reader and writer (useful for testing).
     */
    ProjectRepositoryImpl(ProjectReader reader, ProjectWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public Project load(Path path) {
        var fileProject = reader.read(path);
        FunctionDefinition function = fileProject.function();

        // Use the persisted UUID from the .nfx file; fall back to random
        UUID id;
        try {
            id = (fileProject.id() != null && !fileProject.id().isBlank())
                    ? UUID.fromString(fileProject.id())
                    : UUID.randomUUID();
        } catch (IllegalArgumentException e) {
            id = UUID.randomUUID();
        }

        var domainProject = new Project(
                id,
                fileProject.meta().name(),
                "", // description not stored in .nfx
                function,
                Instant.now(),
                Instant.now()
        );
        log.debug("Loaded project '{}' (id={}) from {}",
                domainProject.name(), id, path);
        return domainProject;
    }

    @Override
    public void save(Project project, Path path) {
        var fileProject = new com.novafx.project.model.Project(
                com.novafx.project.model.Project.CURRENT_VERSION,
                project.id().toString(),
                new Meta(project.name(), ""),
                project.functionDefinition(),
                com.novafx.project.model.ParticleSettings.defaults(),
                com.novafx.project.model.RenderSettings.defaults()
        );
        writer.write(fileProject, path);
        log.debug("Saved project '{}' to {}", project.name(), path);
    }

    @Override
    public ProjectLoadResult loadWithParameters(Path path) {
        var fileProject = reader.read(path);
        FunctionDefinition function = fileProject.function();

        UUID id;
        try {
            id = (fileProject.id() != null && !fileProject.id().isBlank())
                    ? UUID.fromString(fileProject.id())
                    : UUID.randomUUID();
        } catch (IllegalArgumentException e) {
            id = UUID.randomUUID();
        }

        var domainProject = new Project(
                id,
                fileProject.meta().name(),
                "",
                function,
                Instant.now(),
                Instant.now()
        );

        // Convert Map<String, Double> to Map<String, Parameter> for the UI layer
        Map<String, Double> persisted = fileProject.parameters();
        log.debug("Loaded project '{}' (id={}) from {} with {} parameter(s)",
                domainProject.name(), id, path, persisted.size());

        return new ProjectLoadResult(domainProject, persisted);
    }

    @Override
    public void save(Project project, Path path, Map<String, Double> parameters) {
        var fileProject = new com.novafx.project.model.Project(
                com.novafx.project.model.Project.CURRENT_VERSION,
                project.id().toString(),
                new Meta(project.name(), ""),
                project.functionDefinition(),
                parameters != null ? Map.copyOf(parameters) : Map.of(),
                com.novafx.project.model.ParticleSettings.defaults(),
                com.novafx.project.model.RenderSettings.defaults()
        );
        writer.write(fileProject, path);
        log.debug("Saved project '{}' to {} with {} parameter(s)",
                project.name(), path, parameters != null ? parameters.size() : 0);
    }
}
