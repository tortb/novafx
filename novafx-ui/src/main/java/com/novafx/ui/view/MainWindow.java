package com.novafx.ui.view;

import com.novafx.function.Parameter;
import com.novafx.math.FunctionDefinition;
import com.novafx.project.DefaultPlatformService;
import com.novafx.ui.controller.MainController;
import com.novafx.ui.i18n.I18n;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Main application window — NovaFX Studio UI/UX 2.0.
 * <p>
 * Layout:
 * <pre>
 * ┌──────────────────────────────────────────────────┐
 * │ 顶部导航栏 (48px)                                 │
 * ├────────┬──────────────────────┬──────────────────┤
 * │ 左侧    │                      │  属性面板         │
 * │ 资源栏   │     3D 视口         │  粒子/渲染/函数    │
 * │ (280px) │                      │                  │
 * ├────────┴──────────────────────┴──────────────────┤
 * │ 函数编辑器                                        │
 * └──────────────────────────────────────────────────┘
 * </pre>
 */
public final class MainWindow {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 850;

    private final Stage stage;
    private final MainController controller;
    private CommandPalette commandPalette;

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

        stage.setTitle(I18n.get("app.title"));
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);

        setupBindings();
        setupKeyboard(scene);

        controller.applyPreset("Spiral");
        functionEditor.loadDefinition(controller.getCurrentDefinition());

        stage.show();
        log.info("MainWindow opened");
    }

    private BorderPane buildLayout() {
        BorderPane root = new BorderPane();

        // Top: navigation bar
        root.setTop(buildNavBar());

        // Center: 3D viewport
        root.setCenter(canvasViewport);

        // Left: resource panel
        root.setLeft(presetPanel);

        // Right: property tabs + parameters
        VBox rightPanel = new VBox(propertyPanel, parameterPanel);
        rightPanel.setPrefWidth(260);
        root.setRight(rightPanel);

        // Bottom: function editor
        root.setBottom(functionEditor);

        return root;
    }

    private MenuBar buildNavBar() {
        MenuBar menuBar = new MenuBar();

        // ── 文件 ──
        Menu fileMenu = new Menu(I18n.get("menu.file"));
        MenuItem newItem = new MenuItem(I18n.get("menu.file.new"));
        newItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newItem.setOnAction(e -> controller.newProject());

        MenuItem saveItem = new MenuItem(I18n.get("menu.file.save"));
        saveItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveItem.setOnAction(e -> controller.saveProject());

        MenuItem openItem = new MenuItem(I18n.get("menu.file.open"));
        openItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));

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
        fileMenu.getItems().addAll(newItem, saveItem, openItem,
                new SeparatorMenuItem(), exportMenu, new SeparatorMenuItem(), exitItem);

        // ── 视图 ──
        Menu viewMenu = new Menu(I18n.get("menu.view"));
        MenuItem resetCam = new MenuItem(I18n.get("menu.view.resetCamera"));
        resetCam.setOnAction(e -> canvasViewport.resetCamera());
        viewMenu.getItems().add(resetCam);

        // ── 语言 ──
        Menu langMenu = new Menu(I18n.get("menu.language"));
        MenuItem zhItem = new MenuItem(I18n.get("menu.language.zh"));
        zhItem.setOnAction(e -> switchLanguage(Locale.SIMPLIFIED_CHINESE));
        MenuItem enItem = new MenuItem(I18n.get("menu.language.en"));
        enItem.setOnAction(e -> switchLanguage(Locale.ENGLISH));
        langMenu.getItems().addAll(zhItem, enItem);

        // ── 帮助 ──
        Menu helpMenu = new Menu(I18n.get("menu.help"));
        MenuItem aboutItem = new MenuItem(I18n.get("menu.help.about"));
        aboutItem.setOnAction(e -> showAbout());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, langMenu, helpMenu);
        return menuBar;
    }

    private void setupBindings() {
        presetPanel.setOnPresetSelected(name -> {
            FunctionDefinition def = controller.applyPreset(name);
            functionEditor.loadDefinition(def);
            canvasViewport.setPoints(controller.getCurrentPoints());
        });

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

    private void setupKeyboard(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            if (e.isControlDown() && e.isShiftDown() && e.getCode() == KeyCode.P) {
                if (commandPalette == null) {
                    commandPalette = new CommandPalette(
                            () -> controller.newProject(),
                            () -> {},
                            () -> handleExport("CSV (*.csv)", "*.csv", controller::exportCsv),
                            () -> handleExport("JSON (*.json)", "*.json", controller::exportJson),
                            () -> handleExport("MCFunction (*.mcfunction)", "*.mcfunction", controller::exportMcFunction)
                    );
                }
                commandPalette.show();
                e.consume();
            }
        });
    }

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

    private void handleExport(String description, String extension, Consumer<Path> exporter) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("menu.file.export"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            exporter.accept(file.toPath());
        }
    }
}
