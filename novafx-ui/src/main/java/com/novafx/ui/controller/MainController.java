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
import com.novafx.project.DefaultPlatformService;
import com.novafx.project.JacksonProjectRepository;
import com.novafx.project.ProjectRepository;
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
 * manipulation, rendering, project save/load, and export.
 */
public final class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final ProjectRepository projectRepository;
    private final PlatformService platformService;

    private Project currentProject;
    private List<Vector3d> currentPoints;
    private Map<String, Parameter> currentParameters = new LinkedHashMap<>();

    private Runnable onPointsChanged;
    private Runnable onParametersChanged;

    /**
     * Constructs the main controller with default infrastructure.
     */
    public MainController() {
        this.platformService = new DefaultPlatformService();
        this.projectRepository = new JacksonProjectRepository(platformService);
    }

    /**
     * Registers a callback invoked when the point data changes.
     *
     * @param callback a Runnable to execute on point updates
     */
    public void setOnPointsChanged(Runnable callback) {
        this.onPointsChanged = callback;
    }

    /**
     * Registers a callback invoked when the parameter set changes.
     *
     * @param callback a Runnable to execute on parameter updates
     */
    public void setOnParametersChanged(Runnable callback) {
        this.onParametersChanged = callback;
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
        refreshParameters();
        resample();
        return def;
    }

    // ---------------------------------------------------------------
    // Function editing
    // ---------------------------------------------------------------

    /**
     * Updates the current function definition and re-samples.
     *
     * @param xExpr x(t) expression
     * @param yExpr y(t) expression
     * @param zExpr z(t) expression
     * @param start parameter start
     * @param end   parameter end
     * @param step  sampling step
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
     *
     * @param name the preset name
     */
    public void saveCurrentAsPreset(String name) {
        if (currentProject == null) return;
        Project preset = new Project(
                UUID.randomUUID(),
                name,
                "User preset: " + name,
                currentProject.functionDefinition(),
                Instant.now(),
                Instant.now()
        );
        projectRepository.save(preset);
        log.info("Saved preset: {}", name);
    }

    // ---------------------------------------------------------------
    // Parameters
    // ---------------------------------------------------------------

    /**
     * Returns the current set of adjustable parameters.
     *
     * @return immutable list of parameters (never null)
     */
    public List<Parameter> getParameters() {
        return List.copyOf(currentParameters.values());
    }

    /**
     * Updates a single parameter value and re-samples.
     *
     * @param name  the parameter name
     * @param value the new value
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

    /** Saves the current project. */
    public void saveProject() {
        if (currentProject != null) {
            projectRepository.save(currentProject);
            log.info("Saved project: {}", currentProject.id());
        }
    }

    /** Loads a project by ID. */
    public void loadProject(UUID id) {
        projectRepository.findById(id).ifPresent(project -> {
            this.currentProject = project;
            refreshParameters();
            resample();
        });
    }

    /** Creates a new empty project. */
    public void newProject() {
        FunctionDefinition def = new FunctionDefinition("t", "t", "0", 0, 10, 0.5);
        this.currentProject = new Project(
                UUID.randomUUID(), "Untitled", "",
                def, Instant.now(), Instant.now()
        );
        refreshParameters();
        resample();
    }

    // ---------------------------------------------------------------
    // Export
    // ---------------------------------------------------------------

    /** Exports the current project as CSV. */
    public void exportCsv(Path output) {
        exportWith(new CsvExporter(), output);
    }

    /** Exports the current project as JSON. */
    public void exportJson(Path output) {
        exportWith(new JsonExporter(), output);
    }

    /** Exports the current project as Minecraft MCFunction. */
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
}
