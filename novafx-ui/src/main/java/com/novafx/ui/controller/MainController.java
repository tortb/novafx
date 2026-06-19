package com.novafx.ui.controller;

import com.novafx.core.domain.PlatformService;
import com.novafx.core.domain.Project;
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
 * Central controller connecting the UI components with the domain and
 * infrastructure layers.
 * <p>
 * Handles user actions: preset selection, function editing, parameter
 * manipulation, rendering, project save/load/compile, and export.
 */
public final class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final ProjectRepository projectRepository;
    private final ProjectCompiler projectCompiler;
    private final PlatformService platformService;

    private Project currentProject;
    private Path currentProjectPath;
    private List<Vector3d> currentPoints;
    private Map<String, Parameter> currentParameters = new LinkedHashMap<>();

    private Runnable onPointsChanged;
    private Runnable onParametersChanged;
    private Runnable onProjectChanged;

    /**
     * Constructs the main controller with default infrastructure.
     */
    public MainController() {
        this.platformService = new DefaultPlatformService();
        this.projectRepository = new ProjectRepositoryImpl();
        this.projectCompiler = new ProjectCompiler();
    }

    /**
     * Registers a callback invoked when the point data changes.
     */
    public void setOnPointsChanged(Runnable callback) {
        this.onPointsChanged = callback;
    }

    /**
     * Registers a callback invoked when the parameter set changes.
     */
    public void setOnParametersChanged(Runnable callback) {
        this.onParametersChanged = callback;
    }

    /**
     * Registers a callback invoked when the project itself changes
     * (load, new, etc.) — useful for updating the window title.
     */
    public void setOnProjectChanged(Runnable callback) {
        this.onProjectChanged = callback;
    }

    /** Returns the current project, or null. */
    public Project getCurrentProject() {
        return currentProject;
    }

    /** Returns the current project's file path, or null if unsaved. */
    public Path getCurrentProjectPath() {
        return currentProjectPath;
    }

    // ---------------------------------------------------------------
    // Presets
    // ---------------------------------------------------------------

    /** Returns the list of built-in preset names. */
    public List<String> getPresetNames() {
        return MathPresets.names();
    }

    /**
     * Applies a built-in preset by name.
     *
     * @param name the preset name
     * @return the applied FunctionDefinition
     */
    public FunctionDefinition applyPreset(String name) {
        FunctionDefinition def = MathPresets.byName(name);
        if (def == null) {
            throw new IllegalArgumentException("Unknown preset: " + name);
        }
        currentProject = new Project(
                UUID.randomUUID(),
                name,
                "Created from preset: " + name,
                def,
                Instant.now(),
                Instant.now()
        );
        currentProjectPath = null;
        refreshParameters();
        resample();
        fireProjectChanged();
        return def;
    }

    // ---------------------------------------------------------------
    // Function editing
    // ---------------------------------------------------------------

    /**
     * Updates the current function definition and re-samples.
     */
    public void updateFunction(String xExpr, String yExpr, String zExpr,
                               double start, double end, double step) {
        FunctionDefinition def = new FunctionDefinition(xExpr, yExpr, zExpr, start, end, step);
        if (currentProject != null) {
            currentProject = new Project(
                    currentProject.id(),
                    currentProject.name(),
                    currentProject.description(),
                    def,
                    currentProject.createdAt(),
                    Instant.now()
            );
        } else {
            currentProject = new Project(
                    UUID.randomUUID(), "Untitled", "",
                    def, Instant.now(), Instant.now()
            );
        }
        refreshParameters();
        resample();
    }

    /** Returns the current FunctionDefinition, or null. */
    public FunctionDefinition getCurrentDefinition() {
        return currentProject != null ? currentProject.functionDefinition() : null;
    }

    /** Returns the current sampled points, or an empty list. */
    public List<Vector3d> getCurrentPoints() {
        return currentPoints != null ? currentPoints : List.of();
    }

    /**
     * Saves the current function as a named preset.
     */
    public void saveCurrentAsPreset(String name) {
        if (currentProject == null) return;
        // For now, presets are managed in-memory via MathPresets.
        // The old JSON workspace repository has been removed.
        log.info("Preset '{}' would be saved (feature coming)", name);
    }

    // ---------------------------------------------------------------
    // Parameters
    // ---------------------------------------------------------------

    /** Returns the current set of adjustable parameters. */
    public List<Parameter> getParameters() {
        return List.copyOf(currentParameters.values());
    }

    /**
     * Updates a single parameter value and re-samples.
     */
    public void setParameter(String name, double value) {
        Parameter existing = currentParameters.get(name);
        if (existing != null) {
            currentParameters.put(name, existing.withValue(value));
            resample();
        }
    }

    // ---------------------------------------------------------------
    // Project persistence
    // ---------------------------------------------------------------

    /** Creates a new empty project. */
    public void newProject() {
        FunctionDefinition def = new FunctionDefinition("t", "t", "0", 0, 10, 0.5);
        this.currentProject = new Project(
                UUID.randomUUID(), "Untitled", "",
                def, Instant.now(), Instant.now()
        );
        this.currentProjectPath = null;
        refreshParameters();
        resample();
        fireProjectChanged();
    }

    /**
     * Loads a project from a .nfx file path.
     *
     * @param path the path to the .nfx file
     */
    public void loadProject(Path path) {
        Project loaded = projectRepository.load(path);
        this.currentProject = loaded;
        this.currentProjectPath = path;
        refreshParameters();
        resample();
        fireProjectChanged();
        log.info("Loaded project from {}", path);
    }

    /**
     * Saves the current project to its current path.
     *
     * @return true if saved, false if no path is set (caller should prompt for Save As)
     */
    public boolean saveProject() {
        if (currentProject == null) return false;
        if (currentProjectPath == null) return false;
        projectRepository.save(currentProject, currentProjectPath);
        log.info("Saved project to {}", currentProjectPath);
        return true;
    }

    /**
     * Saves the current project to a specified path.
     *
     * @param path the target .nfx path
     */
    public void saveProjectAs(Path path) {
        if (currentProject == null) return;
        this.currentProjectPath = path;
        projectRepository.save(currentProject, path);
        fireProjectChanged();
        log.info("Saved project to {}", path);
    }

    /**
     * Compiles the current project to a .nfxc file.
     *
     * @param output the target .nfxc path
     */
    public void compileProject(Path output) {
        if (currentProject == null) return;
        // Convert domain Project to file-format Project for compilation
        var fileProject = new com.novafx.project.model.Project(
                com.novafx.project.model.Project.CURRENT_VERSION,
                new com.novafx.project.model.Meta(currentProject.name(), ""),
                currentProject.functionDefinition(),
                com.novafx.project.model.ParticleSettings.defaults(),
                com.novafx.project.model.RenderSettings.defaults()
        );
        projectCompiler.compile(fileProject, output);
        log.info("Compiled project to {}", output);
    }

    // ---------------------------------------------------------------
    // Export
    // ---------------------------------------------------------------

    public void exportCsv(Path output) {
        exportWith(new CsvExporter(), output);
    }

    public void exportJson(Path output) {
        exportWith(new JsonExporter(), output);
    }

    public void exportMcFunction(Path output) {
        exportWith(new McFunctionExporter(), output);
    }

    private void exportWith(Exporter exporter, Path output) {
        if (currentProject != null) {
            exporter.export(currentProject, output);
        }
    }

    // ---------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------

    private void refreshParameters() {
        currentParameters.clear();
        FunctionDefinition def = getCurrentDefinition();
        if (def == null) return;

        for (String name : def.parameterNames()) {
            currentParameters.put(name, new Parameter(name, 1.0));
        }

        if (onParametersChanged != null) {
            onParametersChanged.run();
        }
    }

    private void resample() {
        if (currentProject == null) return;

        FunctionDefinition def = currentProject.functionDefinition();
        long count = def.sampleCount();
        if (count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sample count " + count + " exceeds maximum");
        }

        int maxIter = (int) count;
        List<Vector3d> points = new ArrayList<>(maxIter);

        for (int i = 0; i < maxIter; i++) {
            double t = def.start() + i * def.step();
            if (t > def.end() + 1e-12) break;

            Map<String, Double> vars = buildVars(t);
            double x = def.xCompiled().evaluate(vars);
            double y = def.yCompiled().evaluate(vars);
            double z = def.zCompiled().evaluate(vars);
            points.add(new Vector3d(x, y, z));
        }

        this.currentPoints = points;
        log.debug("Sampled {} points for project '{}'", points.size(), currentProject.name());

        if (onPointsChanged != null) {
            onPointsChanged.run();
        }
    }

    private Map<String, Double> buildVars(double t) {
        Map<String, Double> vars = new HashMap<>();
        vars.put("t", t);
        for (Parameter p : currentParameters.values()) {
            vars.put(p.name(), p.value());
        }
        return vars;
    }

    private void fireProjectChanged() {
        if (onProjectChanged != null) {
            onProjectChanged.run();
        }
    }
}
