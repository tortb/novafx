package com.novafx.ui.view;

import com.novafx.function.Parameter;
import com.novafx.math.FunctionDefinition;
import com.novafx.ui.controller.MainController;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.List;

/**
 * The main application window containing the entire NovaFX UI layout.
 * <p>
 * Layout:
 * <pre>
 * ┌───────┬────────────────────┬──────────────┐
 * │       │                    │  Parameters  │
 * │Preset │     Viewport       │  a [slider]  │
 * │Panel  │                    │  b [slider]  │
 * │       │                    │              │
 * ├───────┴────────────────────┴──────────────┤
 * │ Function Editor                           │
 * ├───────────────────────────────────────────┤
 * │ Property Panel                            │
 * └───────────────────────────────────────────┘
 * </pre>
 */
public final class MainWindow {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 800;

    private final Stage stage;
    private final MainController controller;

    private final CanvasViewport canvasViewport;
    private final FunctionEditor functionEditor;
    private final PropertyPanel propertyPanel;
    private final PresetPanel presetPanel;
    private final ParameterPanel parameterPanel;

    /**
     * Creates the main window on the given stage.
     *
     * @param stage the primary stage
     */
    public MainWindow(Stage stage) {
        this.stage = stage;
        this.controller = new MainController();

        // Create UI components
        this.canvasViewport = new CanvasViewport();
        this.functionEditor = new FunctionEditor();
        this.propertyPanel = new PropertyPanel();
        this.presetPanel = new PresetPanel(controller.getPresetNames());
        this.parameterPanel = new ParameterPanel();

        // Build layout
        BorderPane root = buildLayout();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        stage.setTitle("NovaFX Studio — Mathematical Particle Editor");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        // Wire up controller
        setupBindings();

        // Load initial preset
        controller.applyPreset("Spiral");
        functionEditor.loadDefinition(controller.getCurrentDefinition());

        stage.show();
        log.info("MainWindow opened");
    }

    private BorderPane buildLayout() {
        BorderPane root = new BorderPane();

        // Menu bar
        root.setTop(buildMenuBar());

        // Center: 3D viewport
        root.setCenter(canvasViewport);

        // Left: presets
        root.setLeft(presetPanel);

        // Right: parameters
        root.setRight(parameterPanel);

        // Bottom: function editor + properties
        VBox bottom = new VBox(functionEditor, propertyPanel);
        root.setBottom(bottom);

        return root;
    }

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem newProject = new MenuItem("New");
        newProject.setOnAction(e -> controller.newProject());
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> controller.saveProject());
        MenuItem openItem = new MenuItem("Open...");
        openItem.setOnAction(e -> {
            // TODO: file chooser for .nfx files
        });
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> Platform.exit());

        Menu exportMenu = new Menu("Export");
        MenuItem exportCsv = new MenuItem("CSV...");
        exportCsv.setOnAction(e -> handleExport("CSV Files (*.csv)", "*.csv", controller::exportCsv));
        MenuItem exportJson = new MenuItem("JSON...");
        exportJson.setOnAction(e -> handleExport("JSON Files (*.json)", "*.json", controller::exportJson));
        MenuItem exportMc = new MenuItem("MCFunction...");
        exportMc.setOnAction(e -> handleExport("MCFunction Files (*.mcfunction)", "*.mcfunction", controller::exportMcFunction));

        exportMenu.getItems().addAll(exportCsv, exportJson, exportMc);
        fileMenu.getItems().addAll(newProject, saveItem, openItem, new SeparatorMenuItem(), exportMenu, new SeparatorMenuItem(), exitItem);

        // View menu
        Menu viewMenu = new Menu("View");
        MenuItem resetCamera = new MenuItem("Reset Camera");
        resetCamera.setOnAction(e -> canvasViewport.resetCamera());
        viewMenu.getItems().add(resetCamera);

        // Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About NovaFX Studio");
        aboutItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About NovaFX Studio");
            alert.setContentText("NovaFX Studio v1.0\nMathematical Particle Editor for Minecraft");
            alert.show();
        });
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
        return menuBar;
    }

    private void setupBindings() {
        // When user selects a preset
        presetPanel.setOnPresetSelected(name -> {
            FunctionDefinition def = controller.applyPreset(name);
            functionEditor.loadDefinition(def);
            canvasViewport.setPoints(controller.getCurrentPoints());
        });

        // When user edits the function
        functionEditor.setOnFunctionChanged(def -> {
            controller.updateFunction(
                    def.xExpression(), def.yExpression(), def.zExpression(),
                    def.start(), def.end(), def.step()
            );
        });

        // When points are resampled
        controller.setOnPointsChanged(() ->
                Platform.runLater(() ->
                        canvasViewport.setPoints(controller.getCurrentPoints())
                )
        );

        // When parameters change (new set of parameters)
        controller.setOnParametersChanged(() ->
                Platform.runLater(() -> {
                    List<Parameter> params = controller.getParameters();
                    parameterPanel.setParameters(params);
                })
        );

        // When a parameter slider is dragged
        parameterPanel.setOnParameterChanged((name, value) ->
                controller.setParameter(name, value)
        );

        // Property panel bindings
        propertyPanel.setOnPointSizeChanged(size ->
                canvasViewport.setPointSize(size)
        );
        propertyPanel.setOnPointColorChanged(color ->
                canvasViewport.setPointColor(color)
        );
        propertyPanel.setOnShowGridChanged(show ->
                canvasViewport.setShowGrid(show)
        );
    }

    private void handleExport(String description, String extension, Consumer<Path> exporter) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            exporter.accept(file.toPath());
        }
    }
}
