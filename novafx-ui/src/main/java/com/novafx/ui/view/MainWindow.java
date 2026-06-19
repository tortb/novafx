package com.novafx.ui.view;

import com.novafx.core.domain.Project;
import com.novafx.core.error.ProjectError;
import com.novafx.core.workspace.ProjectNode;
import com.novafx.core.workspace.ProjectTreeModel;
import com.novafx.math.FunctionDefinition;
import com.novafx.project.DefaultPlatformService;
import com.novafx.renderer.Camera;
import com.novafx.project.io.NfxcReader;
import com.novafx.project.model.CompiledPointCloud;
import com.novafx.ui.command.UpdateFunctionCommand;
import com.novafx.ui.controller.MainController;
import com.novafx.ui.i18n.I18n;
import com.novafx.ui.view.dialog.NewProjectDialog;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 主窗口 — NovaFX Studio.
 * <p>
 * 布局：
 * <pre>
 * ┌──────────────────────────────────────────────────┐
 * │ TopBar                                            │
 * ├────────┬──────────────────────┬──────────────────┤
 * │ 资源    │                      │  属性面板          │
 * │ 管理器  │     3D 视口          │  参数面板          │
 * │ (220px)│                      │                   │
 * ├────────┴──────────────────────┴──────────────────┤
 * │ ExpressionPanel (表达式输入 + 数学键盘)             │
 * └──────────────────────────────────────────────────┘
 * </pre>
 */
public final class MainWindow {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 850;
    private static final int LEFT_WIDTH = 220;
    private static final int RIGHT_WIDTH = 220;

    private final Stage stage;
    private final MainController controller;

    private final TopBar topBar;
    private final ProjectExplorer projectExplorer;
    private final ExpressionPanel expressionPanel;
    private final CanvasViewport canvasViewport;
    private final PropertyPanel propertyPanel;
    private final ParameterPanel parameterPanel;
    private final PresetPanel presetPanel;
    private final CommandPalette commandPalette;

    private final Label statusLabel = new Label();
    private final VBox rightPanel;

    private boolean leftVisible = true;
    private boolean rightVisible = true;

    /** 创建主窗口。 */
    public MainWindow(Stage stage) {
        this.stage = stage;
        this.controller = new MainController();

        I18n.loadPreference(new DefaultPlatformService().configDirectory());

        this.topBar = new TopBar();
        this.canvasViewport = new CanvasViewport();
        this.expressionPanel = new ExpressionPanel();
        this.propertyPanel = new PropertyPanel();
        this.parameterPanel = new ParameterPanel();

        // Project Explorer
        this.projectExplorer = new ProjectExplorer();

        // Presets (kept for gallery / File > New)
        this.presetPanel = new PresetPanel(controller.getPresetNames());

        // Command palette
        this.commandPalette = new CommandPalette(
                this::handleNew,
                this::handleOpen,
                () -> handleExport("CSV (*.csv)", "*.csv", controller::exportCsv),
                () -> handleExport("JSON (*.json)", "*.json", controller::exportJson),
                () -> handleExport("MCFunction (*.mcfunction)", "*.mcfunction", controller::exportMcFunction)
        );

        // Right panel
        rightPanel = new VBox(0, propertyPanel, parameterPanel);
        rightPanel.setPrefWidth(RIGHT_WIDTH);

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

        // ── 启动：加载预设 "Spiral" 作为首个工程 ──────────
        controller.applyPreset("Spiral");
        expressionPanel.loadDefinition(controller.getCurrentDefinition());

        // 将 workspace 绑定到 explorer 和 command palette
        projectExplorer.setWorkspace(controller.getWorkspace());
        commandPalette.setWorkspace(controller.getWorkspace());
        commandPalette.setOnNavigateToNode(node -> {
            String id = node.id();
            int slash = id.indexOf('/');
            if (slash > 0) {
                String projectId = id.substring(0, slash);
                String subPath = id.substring(slash + 1);
                Platform.runLater(() -> projectExplorer.revealNode(projectId, subPath));
            } else {
                Platform.runLater(() -> projectExplorer.revealNode(id, null));
            }
        });

        // 展开并选中 Function 节点
        Project current = controller.getCurrentProject();
        if (current != null) {
            projectExplorer.revealNode(current.id().toString(), "function");
        }

        stage.show();
        log.info("MainWindow opened (new layout)");
    }

    // ---------------------------------------------------------------
    // 布局
    // ---------------------------------------------------------------

    private BorderPane buildLayout() {
        BorderPane root = new BorderPane();

        root.setTop(topBar);
        root.setLeft(projectExplorer);

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setStyle("-fx-background-color: #0A0A0A;");
        splitPane.getItems().addAll(canvasViewport, expressionPanel);
        splitPane.setDividerPositions(0.70);
        root.setCenter(splitPane);

        root.setRight(rightPanel);

        // 底部状态栏
        HBox statusBar = new HBox(8);
        statusBar.setStyle(
                "-fx-background-color: #0D0D0D;"
                        + "-fx-border-color: #1A1A1A;"
                        + "-fx-border-width: 1 0 0 0;");
        statusBar.setPadding(new javafx.geometry.Insets(2, 8, 2, 8));
        statusLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 10;");
        statusBar.getChildren().add(statusLabel);
        root.setBottom(statusBar);

        return root;
    }

    // ---------------------------------------------------------------
    // 绑定
    // ---------------------------------------------------------------

    private void setupBindings() {
        // ── TopBar ────────────────────────────────────────────
        topBar.setProjectName(controller.getCurrentProject() != null
                ? controller.getCurrentProject().name() : null);
        topBar.setOnNewProject(this::handleNew);
        topBar.setOnOpenProject(this::handleOpen);
        topBar.setOnSaveProject(this::handleSave);
        topBar.setOnUndo(() -> controller.getCommandBus().undo());
        topBar.setOnRedo(() -> controller.getCommandBus().redo());
        topBar.setOnToggleLeftPanel(this::toggleLeftPanel);
        topBar.setOnToggleRightPanel(this::toggleRightPanel);
        topBar.setOn2DMode(() -> {
            canvasViewport.setProjectionMode(CanvasViewport.ProjectionMode.ORTHOGRAPHIC_2D);
            statusLabel.setText("2D 模式");
        });
        topBar.setOn3DMode(() -> {
            canvasViewport.setProjectionMode(CanvasViewport.ProjectionMode.PERSPECTIVE_3D);
            statusLabel.setText("3D 模式");
        });
        topBar.setOnCameraPreset(preset -> {
            var cam = canvasViewport.camera();
            boolean was3d = canvasViewport.getProjectionMode() == CanvasViewport.ProjectionMode.PERSPECTIVE_3D;
            cam.setProjectionType(was3d
                    ? Camera.ProjectionType.PERSPECTIVE
                    : Camera.ProjectionType.ORTHOGRAPHIC);
            switch (preset) {
                case TOP -> { cam.setAzimuth(0).setElevation((float) Math.toRadians(89)).setDistance(8f); }
                case FRONT -> { cam.setAzimuth(0).setElevation(0).setDistance(12f); }
                case SIDE -> { cam.setAzimuth((float) Math.toRadians(90)).setElevation(0).setDistance(12f); }
                case PERSPECTIVE -> { cam.reset(); }
                case ISOMETRIC -> { cam.setAzimuth((float) Math.toRadians(45)).setElevation((float) Math.toRadians(35.264f)).setDistance(12f); }
            }
            statusLabel.setText("视角: " + preset);
        });

        // ── Project Explorer ───────────────────────────────────
        projectExplorer.setOnNodeSelected(node -> {
            statusLabel.setText(I18n.format("status.editing", node.displayName()));
            statusLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 10;");
        });

        projectExplorer.setOnNodeDoubleClicked(node -> navigateToNode(node));

        projectExplorer.setOnNewProject(this::handleNew);

        projectExplorer.setOnContextMenuAction((node, action) -> {
            switch (action) {
                case "close" -> handleCloseProject(node);
                case "save" -> handleContextSave(node);
                case "rename" -> handleContextRename(node);
                case "copyExpr" -> {
                    String expr = node.data();
                    if (expr != null && !expr.isBlank()) {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(expr);
                        clipboard.setContent(content);
                        statusLabel.setText(I18n.format("status.copied", expr));
                        statusLabel.setStyle("-fx-text-fill: #A855F7; -fx-font-size: 10;");
                    }
                }
                case "editParameter" -> {
                    String paramName = node.data();
                    if (paramName != null) {
                        parameterPanel.highlightParameter(paramName);
                        parameterPanel.requestFocus();
                        statusLabel.setText(I18n.get("status.param") + ": " + paramName);
                    }
                }
                case "deleteParameter" -> handleContextDeleteParameter(node);
                case "cameraSettings" -> {
                    canvasViewport.requestFocus();
                    statusLabel.setText(I18n.get("status.camera"));
                    statusLabel.setStyle("-fx-text-fill: #A855F7; -fx-font-size: 10;");
                }
                case "renderSettings" -> {
                    propertyPanel.requestFocus();
                    statusLabel.setText(I18n.get("status.render"));
                    statusLabel.setStyle("-fx-text-fill: #A855F7; -fx-font-size: 10;");
                }
                case "browsePresets" -> showPresetGallery();
                default -> statusLabel.setText(I18n.get("status.editing") + ": " + action);
            }
        });

        // keep preset panel functional (used by File > New)
        presetPanel.setOnPresetSelected(name -> {
            FunctionDefinition def = controller.applyPreset(name);
            expressionPanel.loadDefinition(def);
            canvasViewport.setPoints(controller.getCurrentPoints());
            statusLabel.setText(I18n.format("status.preset", name));
            topBar.setProjectName(controller.getCurrentProject() != null
                    ? controller.getCurrentProject().name() : null);

            Project current = controller.getCurrentProject();
            if (current != null) {
                projectExplorer.revealNode(current.id().toString(), "function");
            }
        });

        presetPanel.setOnSaveCurrentAsPreset(() ->
                controller.saveCurrentAsPreset(controller.getCurrentDefinition().xExpression())
        );

        // ── 表达式面板（通过 CommandBus 支持 Undo）──────────
        expressionPanel.setOnFunctionChanged(def -> {
            var cmd = new UpdateFunctionCommand(controller,
                    def.xExpression(), def.yExpression(), def.zExpression(),
                    def.start(), def.end(), def.step());
            controller.getCommandBus().dispatch(cmd);
        });

        // 即时错误反馈
        expressionPanel.setOnError(err -> {
            Platform.runLater(() -> {
                if (err != null && !err.isBlank()) {
                    statusLabel.setText("⚠ " + err);
                    statusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 10;");
                } else {
                    statusLabel.setText(I18n.get("status.ready"));
                    statusLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 10;");
                }
            });
        });

        // ── 统一状态变更 → 更新全部 UI ─────────────────────
        controller.setOnStateChanged(() ->
                Platform.runLater(() -> {
                    canvasViewport.setPoints(controller.getCurrentPoints());
                    parameterPanel.setParameters(controller.getParameters());
                    var pts = controller.getCurrentPoints();
                    statusLabel.setText(I18n.format("status.points", pts.size()));
                    statusLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 10;");
                    updateTitle();
                    topBar.setProjectName(controller.getCurrentProject() != null
                            ? controller.getCurrentProject().name() : null);
                })
        );

        // ── 管线错误回调 ──────────────────────────────────
        controller.setOnPipelineErrors(errors ->
                Platform.runLater(() -> {
                    for (ProjectError err : errors) {
                        if (err.isError()) {
                            statusLabel.setText("⚠ " + err.message());
                            statusLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 10;");
                            return;
                        }
                    }
                    for (ProjectError err : errors) {
                        if (err.isWarning()) {
                            statusLabel.setText("⚠ " + err.message());
                            statusLabel.setStyle("-fx-text-fill: #A855F7; -fx-font-size: 10;");
                            return;
                        }
                    }
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

    // ---------------------------------------------------------------
    //  节点导航
    // ---------------------------------------------------------------

    private void navigateToNode(ProjectNode node) {
        if (node == null) return;

        switch (node.nodeType()) {
            case X_EXPR -> focusExpression('x', node);
            case Y_EXPR -> focusExpression('y', node);
            case Z_EXPR -> focusExpression('z', node);
            case PARAMETER -> {
                parameterPanel.requestFocus();
                String paramName = node.data();
                if (paramName != null) {
                    parameterPanel.highlightParameter(paramName);
                }
                statusLabel.setText(I18n.get("status.param") + ": " + node.displayName());
            }
            case CAMERA -> {
                canvasViewport.requestFocus();
                statusLabel.setText(I18n.get("status.camera"));
            }
            case RENDER -> {
                propertyPanel.requestFocus();
                statusLabel.setText(I18n.get("status.render"));
            }
            case PRESETS -> showPresetGallery();
            default -> { }
        }
    }

    private void focusExpression(char axis, ProjectNode node) {
        expressionPanel.focusExpression(axis);
        statusLabel.setText(I18n.format("status.editing", node.displayName()));
        statusLabel.setStyle("-fx-text-fill: #A855F7; -fx-font-size: 10;");
    }

    private void showPresetGallery() {
        presetPanel.setOnPresetSelected(name -> {
            FunctionDefinition def = controller.applyPreset(name);
            expressionPanel.loadDefinition(def);
            canvasViewport.setPoints(controller.getCurrentPoints());
            statusLabel.setText(I18n.format("status.preset", name));
            topBar.setProjectName(controller.getCurrentProject() != null
                    ? controller.getCurrentProject().name() : null);
            Project current = controller.getCurrentProject();
            if (current != null) {
                projectExplorer.revealNode(current.id().toString(), "function");
            }
        });
        presetPanel.requestFocus();
        statusLabel.setText(I18n.get("status.browsePresets"));
    }

    // ---------------------------------------------------------------
    // 键盘快捷键
    // ---------------------------------------------------------------

    private void setupKeyboard(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            // Ctrl+P — 命令面板
            if (e.isControlDown() && e.getCode() == KeyCode.P) {
                commandPalette.show();
                e.consume();
            }
            // Ctrl+Z — Undo
            if (e.isControlDown() && !e.isShiftDown() && e.getCode() == KeyCode.Z) {
                controller.getCommandBus().undo();
                e.consume();
            }
            // Ctrl+Shift+Z — Redo
            if (e.isControlDown() && e.isShiftDown() && e.getCode() == KeyCode.Z) {
                controller.getCommandBus().redo();
                e.consume();
            }
            // Ctrl+N — 新建
            if (e.isControlDown() && e.getCode() == KeyCode.N) {
                handleNew();
                e.consume();
            }
            // Ctrl+O — 打开
            if (e.isControlDown() && e.getCode() == KeyCode.O) {
                handleOpen();
                e.consume();
            }
            // Ctrl+S — 保存
            if (e.isControlDown() && e.getCode() == KeyCode.S) {
                handleSave();
                e.consume();
            }
            // Ctrl+B — 编译
            if (e.isControlDown() && e.getCode() == KeyCode.B) {
                handleCompile();
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
        var dialog = new NewProjectDialog();
        var result = dialog.showAndWait(controller.getPresetNames());
        if (result == null) return;

        String template = result.template();
        String projectName = result.projectName();

        FunctionDefinition def;
        if ("Empty".equals(template)) {
            controller.newProject();
        } else {
            def = controller.applyPreset(template);
        }

        controller.renameProject(projectName);

        expressionPanel.loadDefinition(controller.getCurrentDefinition());
        canvasViewport.setPoints(controller.getCurrentPoints());
        expressionPanel.clearError();
        statusLabel.setText(I18n.format("status.newProject", projectName));
        topBar.setProjectName(projectName);

        Project current = controller.getCurrentProject();
        if (current != null) {
            projectExplorer.revealNode(current.id().toString(), "function");
        }
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
            statusLabel.setText(I18n.get("status.saved"));
        } else {
            handleSaveAs();
        }
    }

    private void handleSaveAs() {
        if (controller.getCurrentProject() == null) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("menu.file.saveAs"));
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
            statusLabel.setText(I18n.format("status.savedTo", path.getFileName()));
        }
    }

    private void handleCompile() {
        if (controller.getCurrentProject() == null) return;
        String initialName = controller.getCurrentProject().name()
                .replaceAll("[^a-zA-Z0-9\\-_.]", "_") + ".nfxc";
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("menu.file.compile"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("NovaFX 编译文件 (*.nfxc)", "*.nfxc"));
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            Path path = file.toPath();
            if (!path.toString().endsWith(".nfxc")) {
                path = path.resolveSibling(path.getFileName() + ".nfxc");
            }
            controller.compileProject(path);
            statusLabel.setText(I18n.format("status.compiled", path.getFileName()));
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
            expressionPanel.loadDefinition(controller.getCurrentDefinition());
            canvasViewport.setPoints(controller.getCurrentPoints());
            expressionPanel.clearError();
            statusLabel.setText(I18n.format("status.loaded", path.getFileName()));
            topBar.setProjectName(controller.getCurrentProject() != null
                    ? controller.getCurrentProject().name() : null);

            Project current = controller.getCurrentProject();
            if (current != null) {
                projectExplorer.revealNode(current.id().toString(), "function");
            }
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
                points.add(new com.novafx.math.Vector3d(
                        data[i * 3], data[i * 3 + 1], data[i * 3 + 2]));
            }
            canvasViewport.setPoints(points);
            stage.setTitle("NovaFX Studio — " + path.getFileName() + " (编译)");
            statusLabel.setText(I18n.format("status.loadedCompiled", path.getFileName()));
            log.info("加载编译文件: {}", path);
        } catch (Exception e) {
            log.error("加载编译文件失败: {}", path, e);
            showError("加载编译文件失败: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 工程树上下文操作
    // ---------------------------------------------------------------

    private void handleContextSave(ProjectNode node) {
        var model = findProjectByNodeId(node.id());
        if (model.isEmpty()) return;

        var project = model.get().project();
        var path = model.get().projectPath();

        if (path != null) {
            controller.saveProject(project, path);
            statusLabel.setText(I18n.format("status.savedTo", path.getFileName()));
        } else {
            controller.selectProject(model.get());
            handleSaveAs();
        }
    }

    private void handleContextRename(ProjectNode node) {
        var model = findProjectByNodeId(node.id());
        if (model.isEmpty()) return;

        controller.selectProject(model.get());

        TextInputDialog dialog = new TextInputDialog(model.get().project().name());
        dialog.setTitle("重命名工程");
        dialog.setHeaderText("输入新的工程名称");
        dialog.setContentText("名称:");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                controller.renameProject(name.trim());
                statusLabel.setText(I18n.format("status.renamed", name.trim()));
                topBar.setProjectName(name.trim());
            }
        });
    }

    private void handleContextDeleteParameter(ProjectNode node) {
        String paramName = node.data();
        if (paramName == null || paramName.isBlank()) return;

        var model = findProjectByNodeId(node.id());
        model.ifPresent(m -> controller.selectProject(m));

        controller.removeParameter(paramName);
        statusLabel.setText(I18n.format("status.removedParam", paramName));
        statusLabel.setStyle("-fx-text-fill: #A855F7; -fx-font-size: 10;");
    }

    private java.util.Optional<ProjectTreeModel> findProjectByNodeId(String nodeId) {
        int slash = nodeId.indexOf('/');
        String projectId = slash > 0 ? nodeId.substring(0, slash) : nodeId;
        return controller.getWorkspace().findById(projectId);
    }

    private void handleCloseProject(ProjectNode node) {
        var model = findProjectByNodeId(node.id());
        if (model.isEmpty()) return;

        var ws = controller.getWorkspace();
        boolean wasCurrent = controller.getCurrentProject() != null
                && controller.getCurrentProject().id().toString()
                   .equals(model.get().project().id().toString());

        ws.removeProjectById(model.get().project().id().toString());

        if (wasCurrent) {
            if (ws.isEmpty()) {
                controller.newProject();
            } else {
                controller.selectProject(ws.getProjects().get(0));
            }
            expressionPanel.loadDefinition(controller.getCurrentDefinition());
            canvasViewport.setPoints(controller.getCurrentPoints());
            topBar.setProjectName(controller.getCurrentProject() != null
                    ? controller.getCurrentProject().name() : null);
        }
        statusLabel.setText(I18n.format("status.closed", model.get().project().name()));
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
            statusLabel.setText(I18n.format("status.exported", file.getName()));
        }
    }

    // ---------------------------------------------------------------
    // 面板折叠
    // ---------------------------------------------------------------

    private void toggleLeftPanel() {
        leftVisible = !leftVisible;
        double targetWidth = leftVisible ? LEFT_WIDTH : 0;
        projectExplorer.setPrefWidth(targetWidth);
        projectExplorer.setMinWidth(targetWidth);
        projectExplorer.setVisible(leftVisible);
        projectExplorer.setManaged(leftVisible);
    }

    private void toggleRightPanel() {
        rightVisible = !rightVisible;
        double targetWidth = rightVisible ? RIGHT_WIDTH : 0;
        rightPanel.setPrefWidth(targetWidth);
        rightPanel.setMinWidth(targetWidth);
        rightPanel.setVisible(rightVisible);
        rightPanel.setManaged(rightVisible);
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
