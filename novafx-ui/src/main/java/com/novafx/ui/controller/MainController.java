package com.novafx.ui.controller;

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
import com.novafx.project.compiler.ProjectCompiler;
import com.novafx.project.DefaultPlatformService;
import com.novafx.project.repository.ProjectRepository;
import com.novafx.project.repository.ProjectRepositoryImpl;
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

/**
 * Central controller — single source of truth is {@link ProjectState}.
 * <p>
 * Every mutation creates a new state via copy-on-write, fires
 * {@link #onStateChanged}, and all UI panels read from the one state.
 */
public final class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final ProjectRepository projectRepository;
    private final ProjectCompiler projectCompiler;
    private final PlatformService platformService;

    /** The one source of truth.  Null before any project is loaded. */
    private ProjectState state;

    private final Workspace workspace = new Workspace();

    /** Single callback fired after every state mutation. */
    private Runnable onStateChanged;

    /** True when the function changed since the last sample. */
    private boolean needsResample;

    public MainController() {
        this.platformService = new DefaultPlatformService();
        this.projectRepository = new ProjectRepositoryImpl();
        this.projectCompiler = new ProjectCompiler();
    }

    // ---------------------------------------------------------------
    //  Listener
    // ---------------------------------------------------------------

    /**
     * Registers a single callback fired after <em>every</em> state
     * mutation (replaces the old per-aspect callbacks).
     */
    public void setOnStateChanged(Runnable callback) {
        this.onStateChanged = callback;
    }

    // ---------------------------------------------------------------
    //  State accessors (backward-compat)
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
        if (state == null) return;
        var def = new FunctionDefinition(xExpr, yExpr, zExpr, start, end, step);
        var project = new Project(
                state.project().id(), state.project().name(),
                state.project().description(), def,
                state.project().createdAt(), Instant.now()
        );
        var params = new LinkedHashMap<String, Parameter>();
        for (String pn : def.parameterNames()) {
            params.put(pn, new Parameter(pn, 1.0));
        }
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
    }

    public void loadProject(Path path) {
        var project = projectRepository.load(path);
        var def = project.functionDefinition();
        var params = new LinkedHashMap<String, Parameter>();
        for (String pn : def.parameterNames()) {
            params.put(pn, new Parameter(pn, 1.0));
        }
        var newState = new ProjectState(project, path, List.of(), params);
        needsResample = true;
        setState(newState);
        addCurrentToWorkspace();
        log.info("Loaded project from {}", path);
    }

    public boolean saveProject() {
        if (state == null || state.projectPath() == null) return false;
        projectRepository.save(state.project(), state.projectPath());
        log.info("Saved project to {}", state.projectPath());
        return true;
    }

    public void saveProject(Project project, Path path) {
        if (project == null || path == null) return;
        projectRepository.save(project, path);
        log.info("Saved project '{}' to {}", project.name(), path);
    }

    public void saveProjectAs(Path path) {
        if (state == null) return;
        projectRepository.save(state.project(), path);
        setState(state.withPath(path));
        log.info("Saved project to {}", path);
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

    public void compileProject(Path output) {
        if (state == null) return;
        var fileProject = new com.novafx.project.model.Project(
                com.novafx.project.model.Project.CURRENT_VERSION,
                state.project().id().toString(),
                new com.novafx.project.model.Meta(state.project().name(), ""),
                state.functionDefinition(),
                com.novafx.project.model.ParticleSettings.defaults(),
                com.novafx.project.model.RenderSettings.defaults()
        );
        projectCompiler.compile(fileProject, output);
        log.info("Compiled project to {}", output);
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

    private void addCurrentToWorkspace() {
        if (state == null) return;
        String id = state.project().id().toString();
        if (workspace.findById(id).isPresent()) return;
        var model = ProjectTreeModel.from(state.project(), state.projectPath());
        workspace.addProject(model);
    }
}
