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

/**
 * 主窗口 — NovaFX Studio。
 * <p>
 * 布局：
 * <pre>
 * ┌──────────────────────────────────────────────────┐
 * │ 菜单栏                                             │
 * ├────────┬──────────────────────┬──────────────────┤
 * │ 预设    │                      │  属性面板          │
 * │ (220px) │     3D 视口          │  参数面板          │
 * │         │                      │                   │
 * ├────────┴──────────────────────┴──────────────────┤
 * │ 函数编辑器（简易 / 专业 / LaTeX）                    │
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

    private final Label statusLabel = new Label();

    /** 创建主窗口。 */
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
    // 布局
    // ---------------------------------------------------------------

    private BorderPane buildLayout() {
        BorderPane root = new BorderPane();

        root.setTop(buildMenuBar());
        root.setLeft(presetPanel);

        VBox rightPanel = new VBox(0, propertyPanel, parameterPanel);
        rightPanel.setPrefWidth(220);
        root.setRight(rightPanel);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setStyle("-fx-background-color: #0A0A0A;");
        splitPane.getItems().addAll(canvasViewport, functionEditor);
        splitPane.setDividerPositions(0.75);
        root.setCenter(splitPane);

        // 底部状态栏
        HBox statusBar = new HBox(8);
        statusBar.setStyle("-fx-background-color: #0D0D0D; -fx-border-color: #1A1A1A; -fx-border-width: 1 0 0 0;");
        statusBar.setPadding(new javafx.geometry.Insets(2, 8, 2, 8));
        statusLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 10;");
        statusBar.getChildren().add(statusLabel);
        root.setBottom(statusBar);

        return root;
    }

    // ---------------------------------------------------------------
    // 菜单栏
    // ---------------------------------------------------------------

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();

        // ── 文件 ──
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

        // ── 视图 ──
        Menu viewMenu = new Menu(I18n.get("menu.view"));
        MenuItem resetCam = new MenuItem(I18n.get("menu.view.resetCamera"));
        resetCam.setOnAction(e -> canvasViewport.resetCamera());
        viewMenu.getItems().add(resetCam);

        // ── 帮助 ──
        Menu helpMenu = new Menu(I18n.get("menu.help"));
        MenuItem aboutItem = new MenuItem(I18n.get("menu.help.about"));
        aboutItem.setOnAction(e -> showAbout());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
        return menuBar;
    }

    // ---------------------------------------------------------------
    // 绑定
    // ---------------------------------------------------------------

    private void setupBindings() {
        presetPanel.setOnPresetSelected(name -> {
            FunctionDefinition def = controller.applyPreset(name);
            functionEditor.loadDefinition(def);
            canvasViewport.setPoints(controller.getCurrentPoints());
            statusLabel.setText("预设: " + name);
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

        // 即时错误反馈
        functionEditor.setOnError(err -> {
            Platform.runLater(() -> {
                if (err != null && !err.isBlank()) {
                    statusLabel.setText("⚠ " + err);
                    statusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 10;");
                } else {
                    statusLabel.setText("就绪");
                    statusLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 10;");
                }
            });
        });

        controller.setOnPointsChanged(() ->
                Platform.runLater(() -> {
                    canvasViewport.setPoints(controller.getCurrentPoints());
                    var pts = controller.getCurrentPoints();
                    statusLabel.setText("点云: " + pts.size() + " 点");
                    statusLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 10;");
                })
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
    // 键盘快捷键
    // ---------------------------------------------------------------

    private void setupKeyboard(Scene scene) {
        // Ctrl+P 打开预设面板焦点
        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.P) {
                presetPanel.requestFocus();
                e.consume();
            }
        });
    }

    // ---------------------------------------------------------------
    // 拖拽支持
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
                        log.error("打开拖入文件失败: {}", path, ex);
                        showError("打开失败: " + file.getName());
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    // ---------------------------------------------------------------
    // 文件操作
    // ---------------------------------------------------------------

    private void handleNew() {
        controller.newProject();
        functionEditor.loadDefinition(controller.getCurrentDefinition());
        canvasViewport.setPoints(controller.getCurrentPoints());
        functionEditor.clearErrors();
        statusLabel.setText("新建工程");
    }

    private void handleOpen() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("打开工程");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("NovaFX 工程 (*.nfx)", "*.nfx"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("所有文件", "*.*"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            loadNfxFile(file.toPath());
        }
    }

    private void handleSave() {
        if (controller.getCurrentProjectPath() != null) {
            controller.saveProject();
            statusLabel.setText("已保存");
        } else {
            handleSaveAs();
        }
    }

    private void handleSaveAs() {
        if (controller.getCurrentProject() == null) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("另存为");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("NovaFX 工程 (*.nfx)", "*.nfx"));
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
            statusLabel.setText("已保存: " + path.getFileName());
        }
    }

    private void handleCompile() {
        if (controller.getCurrentProject() == null) return;
        String initialName = controller.getCurrentProject().name()
                .replaceAll("[^a-zA-Z0-9\\-_.]", "_") + ".nfxc";
        FileChooser chooser = new FileChooser();
        chooser.setTitle("编译工程");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("NovaFX 编译文件 (*.nfxc)", "*.nfxc"));
        chooser.setInitialFileName(initialName);
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            Path path = file.toPath();
            if (!path.toString().endsWith(".nfxc")) {
                path = path.resolveSibling(path.getFileName() + ".nfxc");
            }
            controller.compileProject(path);
            statusLabel.setText("已编译: " + path.getFileName());
        }
    }

    private void loadNfxFile(Path path) {
        try {
            Path nfxcPath = path.resolveSibling(
                    path.getFileName().toString().replaceAll("\\.nfx$", ".nfxc"));
            if (Files.exists(nfxcPath)
                    && Files.getLastModifiedTime(nfxcPath)
                    .compareTo(Files.getLastModifiedTime(path)) > 0) {
                loadNfxcFile(nfxcPath);
                return;
            }

            controller.loadProject(path);
            functionEditor.loadDefinition(controller.getCurrentDefinition());
            canvasViewport.setPoints(controller.getCurrentPoints());
            functionEditor.clearErrors();
            statusLabel.setText("已加载: " + path.getFileName());
        } catch (Exception e) {
            log.error("加载工程失败: {}", path, e);
            showError("加载失败: " + e.getMessage());
        }
    }

    private void loadNfxcFile(Path path) {
        try {
            NfxcReader reader = new NfxcReader();
            CompiledPointCloud cloud = reader.read(path);
            var points = new java.util.ArrayList<com.novafx.math.Vector3d>();
            float[] data = cloud.points();
            for (int i = 0; i < cloud.pointCount(); i++) {
                points.add(new com.novafx.math.Vector3d(data[i * 3], data[i * 3 + 1], data[i * 3 + 2]));
            }
            canvasViewport.setPoints(points);
            stage.setTitle("NovaFX Studio — " + path.getFileName() + " (编译)");
            statusLabel.setText("已加载编译文件: " + path.getFileName());
            log.info("加载编译文件: {}", path);
        } catch (Exception e) {
            log.error("加载编译文件失败: {}", path, e);
            showError("加载编译文件失败: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 导出
    // ---------------------------------------------------------------

    private void handleExport(String description, String extension,
                              java.util.function.Consumer<Path> exporter) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("导出");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(description, extension));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            exporter.accept(file.toPath());
            statusLabel.setText("已导出: " + file.getName());
        }
    }

    // ---------------------------------------------------------------
    // 杂项
    // ---------------------------------------------------------------

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于 NovaFX Studio");
        alert.setHeaderText(null);
        alert.setContentText("NovaFX Studio v1.0\n数学粒子编辑器 | 适用于 Minecraft");
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
        String appName = "NovaFX Studio";
        if (projectPath != null) {
            return appName + " — " + projectPath.getFileName();
        }
        if (controller.getCurrentProject() != null) {
            return appName + " — " + controller.getCurrentProject().name() + " (未保存)";
        }
        return appName;
    }
}
