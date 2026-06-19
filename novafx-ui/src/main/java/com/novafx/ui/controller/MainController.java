package com.novafx.ui.controller;

import com.novafx.core.command.CommandBus;
import com.novafx.core.domain.PlatformService;
import com.novafx.core.domain.Project;
import com.novafx.core.state.ProjectState;
import com.novafx.core.workspace.ProjectTreeModel;
import com.novafx.core.workspace.Workspace;
import com.novafx.export.CsvExporter;
import com.novafx.export.Exporter;
import com.novafx.export.JsonExporter;
import com.novafx.export.McFunctionExporter;
import com.novafx.function.Parameter;
import com.novafx.math.FunctionDefinition;
import com.novafx.math.MathPresets;
import com.novafx.math.Vector3d;
import com.novafx.project.DefaultPlatformService;
import com.novafx.project.io.NfxcReader;
import com.novafx.project.io.NfxcWriter;
import com.novafx.project.model.CompiledPointCloud;
import com.novafx.project.pipeline.ProjectPipeline;
import com.novafx.project.repository.ProjectRepository;
import com.novafx.project.repository.ProjectRepositoryImpl;
import com.novafx.project.watcher.ProjectFileWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Central controller — single source of truth is {@link ProjectState}.
 * <p>
 * Every mutation creates a new state via copy-on-write, fires
 * {@link #onStateChanged}, and all UI panels read from the one state.
 */
public final class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final ProjectRepository projectRepository;
    private final ProjectPipeline projectPipeline;
    private final PlatformService platformService;

    /** The one source of truth.  Null before any project is loaded. */
    private ProjectState state;

    private final Workspace workspace = new Workspace();
    private final CommandBus commandBus = new CommandBus();

    /** Single callback fired after every state mutation. */
    private Runnable onStateChanged;

    /** Callback fired when the pipeline produces errors/warnings. */
    private Consumer<List<com.novafx.core.error.ProjectError>> onPipelineErrors;

    /** True when the function changed since the last sample. */
    private boolean needsResample;

    /** File watcher for hot reload (lazily created). */
    private ProjectFileWatcher fileWatcher;

    public MainController() {
        this.platformService = new DefaultPlatformService();
        this.projectRepository = new ProjectRepositoryImpl();
        this.projectPipeline = new ProjectPipeline();
    }

    // ---------------------------------------------------------------
    //  Listeners
    // ---------------------------------------------------------------

    /**
     * Registers a single callback fired after <em>every</em> state
     * mutation.
     */
    public void setOnStateChanged(Runnable callback) {
        this.onStateChanged = callback;
    }

    /**
     * Registers a callback fired when the pipeline produces diagnostics.
     * Receives the full list of errors / warnings / info messages.
     */
    public void setOnPipelineErrors(Consumer<List<com.novafx.core.error.ProjectError>> callback) {
        this.onPipelineErrors = callback;
    }

    // ---------------------------------------------------------------
    //  State accessors
    // ---------------------------------------------------------------

    /** Returns the current state snapshot, or {@code null} if none. */
    public ProjectState getState() {
        return state;
    }

    /** Returns the current project, or null. */
    public Project getCurrentProject() {
        return state != null ? state.project() : null;
    }

    /** Returns the current project's file path, or null if unsaved. */
    public Path getCurrentProjectPath() {
        return state != null ? state.projectPath() : null;
    }

    /** Returns the current FunctionDefinition, or null. */
    public FunctionDefinition getCurrentDefinition() {
        return state != null ? state.functionDefinition() : null;
    }

    /** Returns the current sampled points, or an empty list. */
    public List<Vector3d> getCurrentPoints() {
        return state != null ? state.points() : List.of();
    }

    /** Returns the current set of adjustable parameters. */
    public List<Parameter> getParameters() {
        return state != null
                ? List.copyOf(state.parameters().values())
                : List.of();
    }

    /** Returns the shared workspace (never null). */
    public Workspace getWorkspace() {
        return workspace;
    }

    /** Returns the command bus for undo/redo. */
    public CommandBus getCommandBus() {
        return commandBus;
    }

    /** Returns the file watcher (may be null if not started). */
    public ProjectFileWatcher getFileWatcher() {
        return fileWatcher;
    }

    /**
     * Directly restores a previous state snapshot (used by Command.undo()).
     * Does <em>not</em> go through the CommandBus.
     */
    public void restoreState(ProjectState previous) {
        this.state = previous;
        this.needsResample = false; // already sampled
        if (onStateChanged != null) {
            onStateChanged.run();
        }
    }

    // ---------------------------------------------------------------
    //  State mutation
    // ---------------------------------------------------------------

    /**
     * Atomically replaces the state, re-samples if needed, and fires
     * the listener once.
     */
    private void setState(ProjectState newState) {
        this.state = newState;
        if (needsResample) {
            this.state = resample(newState);
            needsResample = false;
        }
        if (onStateChanged != null) {
            onStateChanged.run();
        }
    }

    // ---------------------------------------------------------------
    //  Presets
    // ---------------------------------------------------------------

    public List<String> getPresetNames() {
        return MathPresets.names();
    }

    /**
     * Applies a built-in preset by name, adds it to the workspace,
     * and sets it as the active project.
     */
    public FunctionDefinition applyPreset(String name) {
        FunctionDefinition def = MathPresets.byName(name);
        if (def == null) {
            throw new IllegalArgumentException("Unknown preset: " + name);
        }
        var project = new Project(
                UUID.randomUUID(), name,
                "Created from preset: " + name, def,
                Instant.now(), Instant.now()
        );
        var params = new LinkedHashMap<String, Parameter>();
        for (String pn : def.parameterNames()) {
            params.put(pn, new Parameter(pn, 1.0));
        }
        var newState = new ProjectState(project, null, List.of(), params);
        needsResample = true;
        setState(newState);
        addCurrentToWorkspace();
        return def;
    }

    // ---------------------------------------------------------------
    //  Function editing
    // ---------------------------------------------------------------

    public void updateFunction(String xExpr, String yExpr, String zExpr,
                               double start, double end, double step) {
        var def = new FunctionDefinition(xExpr, yExpr, zExpr, start, end, step);
        var params = new LinkedHashMap<String, Parameter>();
        for (String pn : def.parameterNames()) {
            params.put(pn, new Parameter(pn, 1.0));
        }
        if (state == null) {
            // Auto-create project when none exists
            var project = new Project(
                    UUID.randomUUID(), "Untitled", "", def,
                    Instant.now(), Instant.now()
            );
            var newState = new ProjectState(project, null, List.of(), params);
            needsResample = true;
            setState(newState);
            return;
        }
        var project = new Project(
                state.project().id(), state.project().name(),
                state.project().description(), def,
                state.project().createdAt(), Instant.now()
        );
        needsResample = true;
        setState(state.withProject(project).withParameters(params));
    }

    // ---------------------------------------------------------------
    //  Parameters
    // ---------------------------------------------------------------

    public void setParameter(String name, double value) {
        if (state == null) return;
        var old = state.parameters();
        if (!old.containsKey(name)) return;
        var updated = new LinkedHashMap<>(old);
        updated.put(name, old.get(name).withValue(value));
        needsResample = true;
        setState(state.withParameters(updated));
    }

    /**
     * Removes a parameter by substituting its current value into all
     * three expressions.
     */
    public void removeParameter(String paramName) {
        if (state == null) return;
        var param = state.parameters().get(paramName);
        if (param == null) return;
        var def = state.functionDefinition();
        String val = formatParamValue(param.value());
        updateFunction(
                def.xExpression().replace(paramName, val),
                def.yExpression().replace(paramName, val),
                def.zExpression().replace(paramName, val),
                def.start(), def.end(), def.step()
        );
    }

    // ---------------------------------------------------------------
    //  Project persistence
    // ---------------------------------------------------------------

    public void newProject() {
        var def = new FunctionDefinition("t", "t", "0", 0, 10, 0.5);
        var project = new Project(
                UUID.randomUUID(), "Untitled", "", def,
                Instant.now(), Instant.now()
        );
        var params = new LinkedHashMap<String, Parameter>();
        for (String pn : def.parameterNames()) {
            params.put(pn, new Parameter(pn, 1.0));
        }
        var newState = new ProjectState(project, null, List.of(), params);
        needsResample = true;
        setState(newState);
        addCurrentToWorkspace();
        stopFileWatcher();
    }

    /**
     * Loads a project from a {@code .nfx} file, including persisted
     * parameter values and cached point cloud if available.
     */
    public void loadProject(Path path) {
        // Load with persisted parameter values
        var result = projectRepository.loadWithParameters(path);
        var project = result.project();
        var def = project.functionDefinition();

        // Use persisted parameter values where available, default to 1.0
        var params = new LinkedHashMap<String, Parameter>();
        Map<String, Double> persistedParams = result.parameters();
        for (String pn : def.parameterNames()) {
            double value = persistedParams.getOrDefault(pn, 1.0);
            params.put(pn, new Parameter(pn, value));
        }

        // Try loading from nfxc cache first
        Path nfxcPath = nfxcPathFor(path);
        List<Vector3d> cachedPoints = tryLoadCachedPoints(nfxcPath, path);

        var newState = new ProjectState(project, path, cachedPoints, params);
        if (cachedPoints == null) {
            needsResample = true;
        }
        setState(newState);
        addCurrentToWorkspace();

        if (cachedPoints != null) {
            log.info("Loaded from cache: {} ({} points)", nfxcPath.getFileName(), cachedPoints.size());
        } else {
            log.info("Loaded project from {}", path);
        }

        // Start watching this project's file for hot reload
        startFileWatcherFor(path);
    }

    public boolean saveProject() {
        if (state == null || state.projectPath() == null) return false;
        saveWithParameters(state.project(), state.projectPath(), state.parameters());
        log.info("Saved project to {}", state.projectPath());
        return true;
    }

    public void saveProject(Project project, Path path) {
        if (project == null || path == null) return;
        Map<String, Double> paramValues = Map.of();
        if (state != null) {
            paramValues = extractParameterValues(state.parameters());
        }
        saveWithParameters(project, path, state != null ? state.parameters() : Map.of());
        log.info("Saved project '{}' to {}", project.name(), path);
    }

    public void saveProjectAs(Path path) {
        if (state == null) return;
        saveWithParameters(state.project(), path, state.parameters());

        // Write nfxc cache alongside
        if (!state.points().isEmpty()) {
            try {
                writeNfxcCache(nfxcPathFor(path), state.points());
            } catch (Exception e) {
                log.warn("Failed to write nfxc cache: {}", e.getMessage());
            }
        }

        setState(state.withPath(path));
        log.info("Saved project to {}", path);

        // Start watching the new file path
        startFileWatcherFor(path);
    }

    /**
     * Internal: saves the project with parameter values persisted in
     * the {@code [parameter]} section of the TOML.
     */
    private void saveWithParameters(Project project, Path path,
                                     Map<String, Parameter> parameters) {
        Map<String, Double> paramValues = extractParameterValues(parameters);
        projectRepository.save(project, path, paramValues);
    }

    public void selectProject(ProjectTreeModel model) {
        var project = model.project();
        var def = project.functionDefinition();
        var params = new LinkedHashMap<String, Parameter>();
        for (String pn : def.parameterNames()) {
            params.put(pn, new Parameter(pn, 1.0));
        }
        var newState = new ProjectState(project, model.projectPath(), List.of(), params);
        needsResample = true;
        setState(newState);
    }

    // ---------------------------------------------------------------
    //  Compilation (with pipeline validation)
    // ---------------------------------------------------------------

    /**
     * Compiles the current project using the full pipeline (validate → sample → cache).
     * <p>
     * Replaces the old manual compilation path.  Pipeline diagnostics are
     * forwarded to the {@link #onPipelineErrors} callback.
     */
    public void compileProject(Path output) {
        if (state == null) return;

        var def = state.functionDefinition();
        var fileProject = new com.novafx.project.model.Project(
                com.novafx.project.model.Project.CURRENT_VERSION,
                state.project().id().toString(),
                new com.novafx.project.model.Meta(state.project().name(), ""),
                def,
                com.novafx.project.model.ParticleSettings.defaults(),
                com.novafx.project.model.RenderSettings.defaults()
        );

        var result = projectPipeline.execute(fileProject, output);

        if (result.success()) {
            // Update state with compiled points from the pipeline
            setState(state.withPoints(result.points()));
            log.info("Pipeline compiled {} points to {}", result.pointCount(), output);
        } else {
            log.warn("Pipeline failed: {}", result.summary());
        }

        // Forward diagnostics
        if (onPipelineErrors != null && !result.errors().isEmpty()) {
            onPipelineErrors.accept(result.errors().getErrors());
        }
    }

    /**
     * Runs pipeline validation only (no compilation) on the current project.
     * Results are forwarded to the error callback.
     */
    public void validateCurrentProject() {
        if (state == null) return;

        var def = state.functionDefinition();
        var fileProject = new com.novafx.project.model.Project(
                com.novafx.project.model.Project.CURRENT_VERSION,
                state.project().id().toString(),
                new com.novafx.project.model.Meta(state.project().name(), ""),
                def,
                com.novafx.project.model.ParticleSettings.defaults(),
                com.novafx.project.model.RenderSettings.defaults()
        );

        var errors = projectPipeline.validateOnly(fileProject);
        if (onPipelineErrors != null && !errors.isEmpty()) {
            onPipelineErrors.accept(errors.getErrors());
        }
    }

    // ---------------------------------------------------------------
    //  Hot reload — file watcher
    // ---------------------------------------------------------------

    /**
     * Starts watching the directory containing {@code nfxPath} for changes.
     * When the file is modified externally, it will be re-loaded automatically.
     */
    private void startFileWatcherFor(Path nfxPath) {
        stopFileWatcher();

        if (nfxPath == null) return;

        Path dir = nfxPath.getParent();
        if (dir == null) return;

        try {
            Path finalNfxPath = nfxPath.toRealPath();
            fileWatcher = new ProjectFileWatcher(changedPath -> {
                if (changedPath.equals(finalNfxPath)) {
                    log.info("Hot reload triggered for {}", changedPath);
                    hotReload(changedPath);
                }
            });
            fileWatcher.watchDirectory(dir);
            fileWatcher.start();
        } catch (Exception e) {
            log.warn("Failed to start file watcher: {}", e.getMessage());
            fileWatcher = null;
        }
    }

    private void stopFileWatcher() {
        if (fileWatcher != null) {
            fileWatcher.stop();
            fileWatcher = null;
        }
    }

    /**
     * Reloads a project from disk when the file changes externally.
     * Preserves the current undo history and workspace selection.
     */
    private void hotReload(Path path) {
        try {
            var result = projectRepository.loadWithParameters(path);
            var project = result.project();
            var def = project.functionDefinition();

            var params = new LinkedHashMap<String, Parameter>();
            Map<String, Double> persistedParams = result.parameters();
            for (String pn : def.parameterNames()) {
                double value = persistedParams.getOrDefault(pn, 1.0);
                params.put(pn, new Parameter(pn, value));
            }

            var newState = new ProjectState(project, path, List.of(), params);
            needsResample = true;
            this.state = newState;
            if (needsResample) {
                this.state = resample(newState);
                needsResample = false;
            }
            if (onStateChanged != null) {
                onStateChanged.run();
            }
            log.info("Hot reloaded project from {}", path);
        } catch (Exception e) {
            log.error("Hot reload failed for {}: {}", path, e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    //  nfxc cache helpers
    // ---------------------------------------------------------------

    /**
     * Returns the sibling .nfxc path for a .nfx file.
     */
    public static Path nfxcPathFor(Path nfxPath) {
        String name = nfxPath.getFileName().toString();
        return nfxPath.resolveSibling(name.replaceAll("\\.nfx$", ".nfxc"));
    }

    /**
     * Tries to load cached points from an .nfxc file that is newer
     * than the source .nfx. Returns null when the cache is missing
     * or stale.
     */
    private static List<Vector3d> tryLoadCachedPoints(Path nfxcPath, Path nfxPath) {
        try {
            if (!java.nio.file.Files.exists(nfxcPath)) return null;
            if (java.nio.file.Files.getLastModifiedTime(nfxcPath)
                    .compareTo(java.nio.file.Files.getLastModifiedTime(nfxPath)) < 0) {
                return null; // cache is stale
            }
            var reader = new NfxcReader();
            CompiledPointCloud cloud = reader.read(nfxcPath);
            var points = new ArrayList<Vector3d>(cloud.pointCount());
            float[] data = cloud.points();
            for (int i = 0; i < cloud.pointCount(); i++) {
                points.add(new Vector3d(data[i * 3], data[i * 3 + 1], data[i * 3 + 2]));
            }
            return points;
        } catch (Exception e) {
            log.warn("Failed to read nfxc cache: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Writes a point cloud to an .nfxc file (binary cache).
     */
    private static void writeNfxcCache(Path nfxcPath, List<Vector3d> points) {
        int n = points.size();
        var data = new float[n * 3];
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            float x = (float) points.get(i).x();
            float y = (float) points.get(i).y();
            float z = (float) points.get(i).z();
            data[i * 3]     = x;
            data[i * 3 + 1] = y;
            data[i * 3 + 2] = z;
            if (x < minX) minX = x; if (x > maxX) maxX = x;
            if (y < minY) minY = y; if (y > maxY) maxY = y;
            if (z < minZ) minZ = z; if (z > maxZ) maxZ = z;
        }
        var writer = new NfxcWriter();
        writer.write(new CompiledPointCloud(data, n,
                minX, minY, minZ, maxX, maxY, maxZ), nfxcPath);
    }

    // ---------------------------------------------------------------
    //  Rename
    // ---------------------------------------------------------------

    public void renameProject(String newName) {
        if (state == null || newName == null || newName.isBlank()) return;
        var p = state.project();
        var renamed = new Project(
                p.id(), newName.trim(), p.description(),
                p.functionDefinition(), p.createdAt(), Instant.now()
        );
        setState(state.withProject(renamed));
    }

    // ---------------------------------------------------------------
    //  Export
    // ---------------------------------------------------------------

    public void exportCsv(Path output)  { exportWith(new CsvExporter(), output); }
    public void exportJson(Path output) { exportWith(new JsonExporter(), output); }
    public void exportMcFunction(Path output) { exportWith(new McFunctionExporter(), output); }

    private void exportWith(Exporter exporter, Path output) {
        if (state != null) exporter.export(state.project(), output);
    }

    public void saveCurrentAsPreset(String name) {
        if (state == null) return;
        log.info("Preset '{}' would be saved (feature coming)", name);
    }

    // ---------------------------------------------------------------
    //  Internal
    // ---------------------------------------------------------------

    private static ProjectState resample(ProjectState s) {
        var def = s.functionDefinition();
        long count = def.sampleCount();
        if (count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sample count " + count + " exceeds maximum");
        }
        int maxIter = (int) count;
        var points = new ArrayList<Vector3d>(maxIter);
        for (int i = 0; i < maxIter; i++) {
            double t = def.start() + i * def.step();
            if (t > def.end() + 1e-12) break;
            Map<String, Double> vars = buildVars(t, s.parameters());
            double x = def.xCompiled().evaluate(vars);
            double y = def.yCompiled().evaluate(vars);
            double z = def.zCompiled().evaluate(vars);
            points.add(new Vector3d(x, y, z));
        }
        log.debug("Sampled {} points", points.size());
        return s.withPoints(points);
    }

    private static String formatParamValue(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private static Map<String, Double> buildVars(double t,
                                                  Map<String, Parameter> params) {
        var vars = new HashMap<String, Double>();
        vars.put("t", t);
        for (var p : params.values()) {
            vars.put(p.name(), p.value());
        }
        return vars;
    }

    private static Map<String, Double> extractParameterValues(
            Map<String, Parameter> parameters) {
        var values = new LinkedHashMap<String, Double>();
        for (var entry : parameters.entrySet()) {
            values.put(entry.getKey(), entry.getValue().value());
        }
        return values;
    }

    private void addCurrentToWorkspace() {
        if (state == null) return;
        String id = state.project().id().toString();
        if (workspace.findById(id).isPresent()) return;
        var model = ProjectTreeModel.from(state.project(), state.projectPath());
        workspace.addProject(model);
    }
}
