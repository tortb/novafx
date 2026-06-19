package com.novafx.project.workspace;

import com.novafx.core.domain.Project;
import com.novafx.core.workspace.ProjectTreeModel;
import com.novafx.core.workspace.Workspace;
import com.novafx.project.repository.ProjectRepository;
import com.novafx.project.repository.ProjectRepositoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Loads {@code .nfx} files from disk and assembles them into a
 * {@link Workspace}.
 * <p>
 * Two usage modes:
 * <ol>
 *   <li><strong>Single-file</strong> — load one {@code .nfx} into
 *       a workspace containing just that project.</li>
 *   <li><strong>Directory scan</strong> — scan a directory for
 *       every {@code *.nfx} file and load each one.</li>
 * </ol>
 * <p>
 * Projects are sorted alphabetically by name within the workspace.
 * Files that fail to parse are logged as warnings and skipped
 * (fail-fast is the caller's responsibility via
 * {@link #loadSingle(Path)}).
 */
public final class WorkspaceLoader {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceLoader.class);

    private final ProjectRepository repository;

    /** Creates a loader with the default repository. */
    public WorkspaceLoader() {
        this(new ProjectRepositoryImpl());
    }

    /**
     * Creates a loader with a custom repository (useful for testing).
     *
     * @param repository the project repository to use
     */
    public WorkspaceLoader(ProjectRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    // ---------------------------------------------------------------
    //  Single-file
    // ---------------------------------------------------------------

    /**
     * Loads a single {@code .nfx} file and returns a workspace
     * containing exactly that one project.
     *
     * @param nfxFile path to a {@code .nfx} file
     * @return a new workspace with the loaded project
     * @throws IllegalArgumentException if the file does not exist,
     *                                  is not a regular file, or
     *                                  doesn't end with {@code .nfx}
     * @throws com.novafx.project.ProjectFormatException if the file
     *                                  is syntactically invalid
     */
    public Workspace loadSingle(Path nfxFile) {
        Objects.requireNonNull(nfxFile, "nfxFile must not be null");
        validateNfxFile(nfxFile);

        Project project = repository.load(nfxFile);
        ProjectTreeModel model = ProjectTreeModel.from(project, nfxFile);

        Workspace workspace = new Workspace();
        workspace.addProject(model);
        log.info("Loaded single project '{}' from {}", project.name(), nfxFile);
        return workspace;
    }

    // ---------------------------------------------------------------
    //  Directory scan
    // ---------------------------------------------------------------

    /**
     * Scans {@code directory} for every file ending in {@code .nfx},
     * loads each one, and returns a workspace containing all
     * successfully-loaded projects.
     * <p>
     * Files that cannot be parsed are logged with a warning and
     * omitted — loading continues.
     *
     * @param directory a readable directory
     * @return a workspace (possibly empty)
     * @throws IllegalArgumentException if the path is not a readable directory
     */
    public Workspace loadDirectory(Path directory) {
        Objects.requireNonNull(directory, "directory must not be null");
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException(
                    "Not a readable directory: " + directory);
        }

        List<Path> nfxFiles = scanNfxFiles(directory);

        if (nfxFiles.isEmpty()) {
            log.info("No .nfx files found in {}", directory);
            return new Workspace();
        }

        List<ProjectTreeModel> models = new ArrayList<>(nfxFiles.size());

        for (Path file : nfxFiles) {
            try {
                Project project = repository.load(file);
                models.add(ProjectTreeModel.from(project, file));
                log.debug("  Loaded '{}' from {}", project.name(), file);
            } catch (Exception e) {
                log.warn("Skipping invalid .nfx file: {} ({})",
                        file.getFileName(), e.getMessage());
            }
        }

        // Sort alphabetically by project name for a stable display
        models.sort(Comparator.comparing(
                m -> m.project().name(), String.CASE_INSENSITIVE_ORDER));

        Workspace workspace = new Workspace();
        models.forEach(workspace::addProject);
        log.info("Loaded {} project(s) from {}", models.size(), directory);
        return workspace;
    }

    // ---------------------------------------------------------------
    //  Internals
    // ---------------------------------------------------------------

    private static void validateNfxFile(Path file) {
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("File does not exist: " + file);
        }
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Not a regular file: " + file);
        }
        if (!file.getFileName().toString().toLowerCase().endsWith(".nfx")) {
            throw new IllegalArgumentException(
                    "File must have a .nfx extension: " + file);
        }
    }

    /**
     * Scans a directory for {@code *.nfx} files.
     */
    private static List<Path> scanNfxFiles(Path directory) {
        List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(directory, "*.nfx")) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    result.add(entry);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to scan directory {}: {}", directory, e.getMessage());
        }
        return result;
    }
}
