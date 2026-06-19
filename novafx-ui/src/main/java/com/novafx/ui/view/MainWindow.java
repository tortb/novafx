package com.novafx.ui.view;

import com.novafx.function.Parameter;
import com.novafx.math.FunctionDefinition;
import com.novafx.project.DefaultPlatformService;
import com.novafx.project.io.NfxcReader;
import com.novafx.project.model.CompiledPointCloud;
import com.novafx.ui.controller.MainController;
import com.novafx.ui.i18n.I18n;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Main application window — NovaFX Studio.
 * <p>
 * Layout:
 * <pre>
 * ┌──────────────────────────────────────────────────┐
 * │ Menu Bar                                          │
 * ├────────┬──────────────────────┬──────────────────┤
 * │ Left    │                      │  Property Panel   │
 * │ Presets │     3D Viewport      │  Parameter Panel  │
 * │ (280px) │                      │                   │
 * ├────────┴──────────────────────┴──────────────────┤
 * │ Function Editor                                   │
 * └──────────────────────────────────────────────────┘
 * </pre>
 */
public final class MainWindow {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 850;

    private final Stage stage;
    private final MainController controller;

    private final CanvasViewport canvasViewport;
    private final FunctionEditor functionEditor;
    private final PropertyPanel propertyPanel;
    private final PresetPanel presetPanel;
    private final ParameterPanel parameterPanel;

    /** Creates the main window on the given stage. */
    public MainWindow(Stage stage) {
        this.stage = stage;
        this.controller = new MainController();

        I18n.loadPreference(new DefaultPlatformService().configDirectory());

        this.canvasViewport = new CanvasViewport();
        this.functionEditor = new FunctionEditor();
        this.propertyPanel = new PropertyPanel();
        this.presetPanel = new PresetPanel(controller.getPresetNames());
        this.parameterPanel = new ParameterPanel();

        BorderPane root = buildLayout();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Apply theme CSS
        var cssUrl = getClass().getResource("/theme/novafx-dark.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle(buildTitle(null));
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);

        setupBindings();
        setupKeyboard(scene);
        setupDragDrop(scene);

        controller.applyPreset("Spiral");
        functionEditor.loadDefinition(controller.getCurrentDefinition());

        stage.show();
        log.info("MainWindow opened");
    }

    // ---------------------------------------------------------------
    // Layout
    // ---------------------------------------------------------------

    private BorderPane buildLayout() {
        BorderPane root = new BorderPane();

        // Top: menu bar
        root.setTop(buildMenuBar());

        // Left: resource panel
        root.setLeft(presetPanel);

        // Right: property tabs + parameters
        VBox rightPanel = new VBox(propertyPanel, parameterPanel);
        rightPanel.setPrefWidth(260);
        root.setRight(rightPanel);

        // Center: vertical split between viewport and function editor
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setStyle("-fx-background-color: #0A0A0A;");

        splitPane.getItems().addAll(canvasViewport, functionEditor);
        splitPane.setDividerPositions(0.75);

        root.setCenter(splitPane);

        return root;
    }

    // ---------------------------------------------------------------
    // Menu Bar
    // ---------------------------------------------------------------

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();

        // ── File ──
        Menu fileMenu = new Menu(I18n.get("menu.file"));

        MenuItem newItem = new MenuItem(I18n.get("menu.file.new"));
        newItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newItem.setOnAction(e -> handleNew());

        MenuItem openItem = new MenuItem(I18n.get("menu.file.open"));
        openItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        openItem.setOnAction(e -> handleOpen());

        MenuItem saveItem = new MenuItem(I18n.get("menu.file.save"));
        saveItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveItem.setOnAction(e -> handleSave());

        MenuItem saveAsItem = new MenuItem(I18n.get("menu.file.saveAs"));
        saveAsItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S"));
        saveAsItem.setOnAction(e -> handleSaveAs());

        MenuItem compileItem = new MenuItem(I18n.get("menu.file.compile"));
        compileItem.setAccelerator(KeyCombination.keyCombination("Ctrl+B"));
        compileItem.setOnAction(e -> handleCompile());

        Menu exportMenu = new Menu(I18n.get("menu.file.export"));
        MenuItem csvItem = new MenuItem(I18n.get("menu.file.export.csv"));
        csvItem.setOnAction(e -> handleExport("CSV (*.csv)", "*.csv", controller::exportCsv));
        MenuItem jsonItem = new MenuItem(I18n.get("menu.file.export.json"));
        jsonItem.setOnAction(e -> handleExport("JSON (*.json)", "*.json", controller::exportJson));
        MenuItem mcItem = new MenuItem(I18n.get("menu.file.export.mcfunction"));
        mcItem.setOnAction(e -> handleExport("MCFunction (*.mcfunction)", "*.mcfunction", controller::exportMcFunction));
        exportMenu.getItems().addAll(csvItem, jsonItem, mcItem);

        MenuItem exitItem = new MenuItem(I18n.get("menu.file.exit"));
        exitItem.setOnAction(e -> Platform.exit());

        fileMenu.getItems().addAll(
                newItem, openItem, saveItem, saveAsItem,
                new SeparatorMenuItem(), compileItem,
                new SeparatorMenuItem(), exportMenu,
                new SeparatorMenuItem(), exitItem
        );

        // ── View ──
        Menu viewMenu = new Menu(I18n.get("menu.view"));
        MenuItem resetCam = new MenuItem(I18n.get("menu.view.resetCamera"));
        resetCam.setOnAction(e -> canvasViewport.resetCamera());
        viewMenu.getItems().add(resetCam);

        // ── Language ──
        Menu langMenu = new Menu(I18n.get("menu.language"));
        MenuItem zhItem = new MenuItem(I18n.get("menu.language.zh"));
        zhItem.setOnAction(e -> switchLanguage(Locale.SIMPLIFIED_CHINESE));
        MenuItem enItem = new MenuItem(I18n.get("menu.language.en"));
        enItem.setOnAction(e -> switchLanguage(Locale.ENGLISH));
        langMenu.getItems().addAll(zhItem, enItem);

        // ── Help ──
        Menu helpMenu = new Menu(I18n.get("menu.help"));
        MenuItem aboutItem = new MenuItem(I18n.get("menu.help.about"));
        aboutItem.setOnAction(e -> showAbout());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, langMenu, helpMenu);
        return menuBar;
    }

    // ---------------------------------------------------------------
    // Bindings
    // ---------------------------------------------------------------

    private void setupBindings() {
        presetPanel.setOnPresetSelected(name -> {
            FunctionDefinition def = controller.applyPreset(name);
            functionEditor.loadDefinition(def);
            canvasViewport.setPoints(controller.getCurrentPoints());
        });

        presetPanel.setOnSaveCurrentAsPreset(() ->
                controller.saveCurrentAsPreset(controller.getCurrentDefinition().xExpression())
        );

        functionEditor.setOnFunctionChanged(def -> {
            controller.updateFunction(
                    def.xExpression(), def.yExpression(), def.zExpression(),
                    def.start(), def.end(), def.step()
            );
        });

        controller.setOnPointsChanged(() ->
                Platform.runLater(() ->
                        canvasViewport.setPoints(controller.getCurrentPoints())
                )
        );

        controller.setOnParametersChanged(() ->
                Platform.runLater(() -> {
                    List<Parameter> params = controller.getParameters();
                    parameterPanel.setParameters(params);
                })
        );

        controller.setOnProjectChanged(() ->
                Platform.runLater(this::updateTitle)
        );

        parameterPanel.setOnParameterChanged((name, value) ->
                controller.setParameter(name, value)
        );

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

    // ---------------------------------------------------------------
    // Keyboard shortcuts
    // ---------------------------------------------------------------

    private void setupKeyboard(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            if (e.isControlDown() && e.isShiftDown() && e.getCode() == KeyCode.P) {
                // Command palette — simplified
                e.consume();
            }
        });
    }

    // ---------------------------------------------------------------
    // Drag-and-Drop
    // ---------------------------------------------------------------

    private void setupDragDrop(Scene scene) {
        scene.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".nfx") || name.endsWith(".nfxc")) {
                        event.acceptTransferModes(TransferMode.COPY);
                        break;
                    }
                }
            }
            event.consume();
        });

        scene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    Path path = file.toPath();
                    String name = file.getName().toLowerCase();
                    try {
                        if (name.endsWith(".nfx")) {
                            loadNfxFile(path);
                            success = true;
                        } else if (name.endsWith(".nfxc")) {
                            loadNfxcFile(path);
                            success = true;
                        }
                    } catch (Exception ex) {
                        log.error("Failed to open dropped file: {}", path, ex);
                        showError("Failed to open: " + file.getName());
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    // ---------------------------------------------------------------
    // File operations
    // ---------------------------------------------------------------

    private void handleNew() {
        controller.newProject();
        functionEditor.loadDefinition(controller.getCurrentDefinition());
        canvasViewport.setPoints(controller.getCurrentPoints());
    }

    private void handleOpen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("menu.file.open"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("NovaFX Project (*.nfx)", "*.nfx"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            loadNfxFile(file.toPath());
        }
    }

    private void handleSave() {
        if (controller.getCurrentProjectPath() != null) {
            controller.saveProject();
        } else {
            handleSaveAs();
        }
    }

    private void handleSaveAs() {
        if (controller.getCurrentProject() == null) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("menu.file.saveAs"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("NovaFX Project (*.nfx)", "*.nfx"));
        // Suggest the project name as default file name
        String initialName = controller.getCurrentProject().name()
                .replaceAll("[^a-zA-Z0-9\\-_.]", "_") + ".nfx";
        chooser.setInitialFileName(initialName);
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            Path path = file.toPath();
            if (!path.toString().endsWith(".nfx")) {
                path = path.resolveSibling(path.getFileName() + ".nfx");
            }
            controller.saveProjectAs(path);
            updateTitle();
        }
    }

    private void handleCompile() {
        if (controller.getCurrentProject() == null) return;
        String initialName = controller.getCurrentProject().name()
                .replaceAll("[^a-zA-Z0-9\\-_.]", "_") + ".nfxc";
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("menu.file.compile"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("NovaFX Compiled (*.nfxc)", "*.nfxc"));
        chooser.setInitialFileName(initialName);
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            Path path = file.toPath();
            if (!path.toString().endsWith(".nfxc")) {
                path = path.resolveSibling(path.getFileName() + ".nfxc");
            }
            controller.compileProject(path);
        }
    }

    private void loadNfxFile(Path path) {
        try {
            // Auto-cache: check if .nfxc exists and is newer
            Path nfxcPath = path.resolveSibling(
                    path.getFileName().toString().replaceAll("\\.nfx$", ".nfxc"));
            if (Files.exists(nfxcPath)
                    && Files.getLastModifiedTime(nfxcPath)
                    .compareTo(Files.getLastModifiedTime(path)) > 0) {
                // Compiled file is newer — load that instead
                loadNfxcFile(nfxcPath);
                return;
            }

            controller.loadProject(path);
            functionEditor.loadDefinition(controller.getCurrentDefinition());
            canvasViewport.setPoints(controller.getCurrentPoints());
        } catch (Exception e) {
            log.error("Failed to load project: {}", path, e);
            showError("Failed to load: " + e.getMessage());
        }
    }

    private void loadNfxcFile(Path path) {
        try {
            NfxcReader reader = new NfxcReader();
            CompiledPointCloud cloud = reader.read(path);
            // Convert float buffer to Vector3d list for the viewport
            var points = new java.util.ArrayList<com.novafx.math.Vector3d>();
            float[] data = cloud.points();
            for (int i = 0; i < cloud.pointCount(); i++) {
                points.add(new com.novafx.math.Vector3d(data[i * 3], data[i * 3 + 1], data[i * 3 + 2]));
            }
            canvasViewport.setPoints(points);

            // Update title to show compiled file
            stage.setTitle("NovaFX Studio — " + path.getFileName() + " (compiled)");
            log.info("Loaded compiled file: {}", path);
        } catch (Exception e) {
            log.error("Failed to load compiled file: {}", path, e);
            showError("Failed to load compiled file: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Export
    // ---------------------------------------------------------------

    private void handleExport(String description, String extension, java.util.function.Consumer<Path> exporter) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("menu.file.export"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            exporter.accept(file.toPath());
        }
    }

    // ---------------------------------------------------------------
    // Misc
    // ---------------------------------------------------------------

    private void switchLanguage(Locale locale) {
        I18n.setLocale(locale);
        I18n.savePreference(new DefaultPlatformService().configDirectory());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("NovaFX");
        alert.setHeaderText(null);
        alert.setContentText("请重启应用以生效。\nPlease restart the application.");
        alert.showAndWait();
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.get("app.about.title"));
        alert.setHeaderText(null);
        alert.setContentText(I18n.get("app.about.text"));
        alert.show();
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("NovaFX");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    private void updateTitle() {
        stage.setTitle(buildTitle(controller.getCurrentProjectPath()));
    }

    private String buildTitle(Path projectPath) {
        String appName = I18n.get("app.title");
        if (projectPath != null) {
            return appName + " — " + projectPath.getFileName();
        }
        if (controller.getCurrentProject() != null) {
            return appName + " — " + controller.getCurrentProject().name() + " (unsaved)";
        }
        return appName;
    }
}
