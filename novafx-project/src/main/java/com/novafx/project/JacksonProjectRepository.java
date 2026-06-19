package com.novafx.project;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.novafx.core.domain.PlatformService;
import com.novafx.core.domain.Project;
import com.novafx.math.FunctionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Jackson-backed {@link ProjectRepository} that persists projects as
 * {@code .nfx} JSON files.
 * <p>
 * Files are stored in {@code workspace/projects} under the platform-specific
 * data directory. Each file is named {@code <uuid>.nfx}.
 */
public final class JacksonProjectRepository implements ProjectRepository {

    private static final Logger log = LoggerFactory.getLogger(JacksonProjectRepository.class);
    private static final String FILE_SUFFIX = ".nfx";
    private static final String FILE_VERSION = "1.0";

    private final Path storageDir;
    private final ObjectMapper mapper;

    /**
     * Constructs a repository using the given platform service for
     * path resolution.
     *
     * @param platformService platform-specific path provider
     */
    public JacksonProjectRepository(PlatformService platformService) {
        this.storageDir = platformService.workspaceDirectory();
        this.mapper = JsonMapper.builder()
                .findAndAddModules()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        ensureDirectoryExists();
    }

    /**
     * Constructs a repository with an explicit storage directory
     * (useful for testing).
     *
     * @param storageDir directory to store .nfx files
     */
    JacksonProjectRepository(Path storageDir) {
        this.storageDir = storageDir;
        this.mapper = JsonMapper.builder()
                .findAndAddModules()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        ensureDirectoryExists();
    }

    @Override
    public void save(Project project) {
        var fileData = new NovaFxProjectFile(
                FILE_VERSION,
                project.id(),
                project.name(),
                project.description(),
                project.createdAt(),
                project.updatedAt(),
                NovaFxProjectFile.FunctionData.from(project.functionDefinition())
        );
        Path filePath = resolvePath(project.id());
        try {
            mapper.writeValue(filePath.toFile(), fileData);
            log.debug("Saved project {} to {}", project.id(), filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save project " + project.id(), e);
        }
    }

    @Override
    public Optional<Project> findById(UUID id) {
        Path filePath = resolvePath(id);
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        return Optional.ofNullable(readFile(filePath));
    }

    @Override
    public List<Project> findAll() {
        List<Project> projects = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageDir, "*" + FILE_SUFFIX)) {
            for (Path path : stream) {
                Project p = readFile(path);
                if (p != null) {
                    projects.add(p);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to list projects in {}", storageDir, e);
        }
        return List.copyOf(projects);
    }

    @Override
    public boolean deleteById(UUID id) {
        Path filePath = resolvePath(id);
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.debug("Deleted project {}", id);
            }
            return deleted;
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete project " + id, e);
        }
    }

    @Override
    public long count() {
        return findAll().size();
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    private Path resolvePath(UUID id) {
        return storageDir.resolve(id.toString() + FILE_SUFFIX);
    }

    private Project readFile(Path path) {
        try {
            NovaFxProjectFile fileData = mapper.readValue(path.toFile(), NovaFxProjectFile.class);
            FunctionDefinition funcDef = fileData.function().toFunctionDefinition();
            return new Project(
                    fileData.id(),
                    fileData.name(),
                    fileData.description(),
                    funcDef,
                    fileData.createdAt(),
                    fileData.updatedAt()
            );
        } catch (IOException e) {
            log.warn("Failed to read project file {}: {}", path, e.getMessage());
            return null;
        }
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory: " + storageDir, e);
        }
    }
}
