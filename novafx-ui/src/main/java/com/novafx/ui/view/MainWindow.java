package com.novafx.ui.view;

import com.novafx.core.domain.Project;
import com.novafx.core.workspace.ProjectNode;
import com.novafx.core.workspace.ProjectNodeType;
import com.novafx.core.workspace.ProjectTreeModel;
import com.novafx.core.workspace.Workspace;
import com.novafx.function.Parameter;
import com.novafx.math.FunctionDefinition;
import com.novafx.project.DefaultPlatformService;
import com.novafx.project.io.NfxcReader;
import com.novafx.project.model.CompiledPointCloud;
import com.novafx.project.workspace.WorkspaceLoader;
import com.novafx.ui.controller.MainController;
import com.novafx.ui.i18n.I18n;
import com.novafx.ui.view.dialog.NewProjectDialog;
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
 * 主窗口 — NovaFX Studio.
 * <p>
 * 布局：
 * <pre>
 * ┌──────────────────────────────────────────────────┐
 * │ 菜单栏                                             │
 * ├────────┬──────────────────────┬──────────────────┤
 * │ 资源    │                      │  属性面板          │
 * │ 管理器  │     3D 视口          │  参数面板          │
 * │ (220px)│                      │                   │
 * ├────────┴──────────────────────┴──────────────────┤
 * │ 函数编辑器（简易 / 专业 / LaTeX）                    │
 * └──────────────────────────────────────────────────┘
 * </pre>
 * <p>
 * 左侧 {@link ProjectExplorer} 管理 .nfx 工程树，替代旧版预设卡片面板。
 */
public final class MainWindow {

    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 850;

    private final Stage stage;
    private final MainController controller;

    private final ProjectExplorer projectExplorer;
    private final CanvasViewport canvasViewport;
    private final FunctionEditor functionEditor;
    private final PropertyPanel propertyPanel;
    private final PresetPanel presetPanel;
    private final ParameterPanel parameterPanel;
    private final CommandPalette commandPalette;

    private final Label statusLabel = new Label();

    /** 创建主窗口。 */
    public MainWindow(Stage stage) {
        this.stage = stage;
        this.controller = new MainController();

        I18n.loadPreference(new DefaultPlatformService().configDirectory());

        this.canvasViewport = new CanvasViewport();
        this.functionEditor = new FunctionEditor();
        this.propertyPanel = new PropertyPanel();
        this.parameterPanel = new ParameterPanel();

        // Project Explorer (replaces old preset grid as the left panel)
        this.projectExplorer = new ProjectExplorer();
        this.presetPanel = new PresetPanel(controller.getPresetNames());

        this.commandPalette = new CommandPalette(
                this::handleNew,
                this::handleOpen,
                () -> handleExport("CSV (*.csv)", "*.csv", controller::exportCsv),
                () -> handleExport("JSON (*.json)", "*.json", controller::exportJson),
                () -> handleExport("MCFunction (*.mcfunction)", "*.mcfunction", controller::exportMcFunction)
        );

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
        functionEditor.loadDefinition(controller.getCurrentDefinition());

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

        // 展开并选中 Function 节点，方便查看
        Project current = controller.getCurrentProject();
        if (current != null) {
            projectExplorer.revealNode(current.id().toString(), "function");
        }

        stage.show();
        log.info("MainWindow opened");
    }

    // ---------------------------------------------------------------
    // 布局
    // ---------------------------------------------------------------

    private BorderPane buildLayout() {
        BorderPane root = new BorderPane();

        root.setTop(buildMenuBar());
        root.setLeft(projectExplorer);

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
                        javafx.scene.input.Clipboard clipboard =
                                javafx.scene.input.Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(expr);
                        clipboard.setContent(content);
                        statusLabel.setText(I18n.format("status.copied", expr));
                        statusLabel.setStyle("-fx-text-fill: #F97316; -fx-font-size: 10;");
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
                    statusLabel.setStyle("-fx-text-fill: #F97316; -fx-font-size: 10;");
                }
                case "renderSettings" -> {
                    propertyPanel.requestFocus();
                    statusLabel.setText(I18n.get("status.render"));
                    statusLabel.setStyle("-fx-text-fill: #F97316; -fx-font-size: 10;");
                }
                case "browsePresets" -> showPresetGallery();
                default -> statusLabel.setText(I18n.get("status.editing") + ": " + action);
            }
        });

        // keep preset panel functional (used by File > New menu)
        presetPanel.setOnPresetSelected(name -> {
            FunctionDefinition def = controller.applyPreset(name);
            functionEditor.loadDefinition(def);
            canvasViewport.setPoints(controller.getCurrentPoints());
            statusLabel.setText(I18n.format("status.preset", name));

            // reveal in explorer
            Project current = controller.getCurrentProject();
            if (current != null) {
                projectExplorer.revealNode(current.id().toString(), "function");
            }
        });

        presetPanel.setOnSaveCurrentAsPreset(() ->
                controller.saveCurrentAsPreset(controller.getCurrentDefinition().xExpression())
        );

        // ── 函数编辑器 ──────────────────────────────────────
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
                    statusLabel.setText(I18n.get("status.ready"));
                    statusLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 10;");
                }
            });
        });

        // ── 其它面板 ──────────────────────────────────────
        controller.setOnPointsChanged(() ->
                Platform.runLater(() -> {
                    canvasViewport.setPoints(controller.getCurrentPoints());
                    var pts = controller.getCurrentPoints();
                    statusLabel.setText(I18n.format("status.points", pts.size()));
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
    //  节点导航 — 双击树节点时的行为
    // ---------------------------------------------------------------

    private void navigateToNode(ProjectNode node) {
        if (node == null) return;

        switch (node.nodeType()) {
            case X_EXPR -> focusExpression('x', node);
            case Y_EXPR -> focusExpression('y', node);
            case Z_EXPR -> focusExpression('z', node);
            case PARAMETER -> {
                // 跳转到参数面板
                parameterPanel.requestFocus();
                // 尝试选中对应参数控件
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
            case PRESETS -> {
                // 显示预设选择对话框
                showPresetGallery();
            }
            default -> {
                // 容器节点仅展开/折叠, 不导航
            }
        }
    }

    private void focusExpression(char axis, ProjectNode node) {
        functionEditor.focusExpression(axis);
        statusLabel.setText(I18n.format("status.editing", node.displayName()));
        statusLabel.setStyle("-fx-text-fill: #F97316; -fx-font-size: 10;");
    }

    private void showPresetGallery() {
        // 在当前窗口展示预设选择
        // 复用选定的回调
        presetPanel.setOnPresetSelected(name -> {
            FunctionDefinition def = controller.applyPreset(name);
            functionEditor.loadDefinition(def);
            canvasViewport.setPoints(controller.getCurrentPoints());
            statusLabel.setText(I18n.format("status.preset", name));
            Project current = controller.getCurrentProject();
            if (current != null) {
                projectExplorer.revealNode(current.id().toString(), "function");
            }
        });
        // 触发预设面板的焦点 (或可改为弹出窗口)
        presetPanel.requestFocus();
        statusLabel.setText(I18n.get("status.browsePresets"));
    }

    // ---------------------------------------------------------------
    // 键盘快捷键
    // ---------------------------------------------------------------

    private void setupKeyboard(Scene scene) {
        // Ctrl+P 打开命令面板（搜索节点 / 命令）
        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.P) {
                commandPalette.show();
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
            def = new FunctionDefinition("t", "t", "0", 0, 10, 0.5);
            controller.newProject();
        } else {
            def = controller.applyPreset(template);
        }

        // Apply the user-chosen project name
        controller.renameProject(projectName);

        functionEditor.loadDefinition(controller.getCurrentDefinition());
        canvasViewport.setPoints(controller.getCurrentPoints());
        functionEditor.clearErrors();
        statusLabel.setText(I18n.format("status.newProject", projectName));

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
            functionEditor.loadDefinition(controller.getCurrentDefinition());
            canvasViewport.setPoints(controller.getCurrentPoints());
            functionEditor.clearErrors();
            statusLabel.setText(I18n.format("status.loaded", path.getFileName()));

            // reveal in explorer
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

    /**
     * Saves the project that owns the given tree node.
     * If the project has no file path, delegates to Save-As.
     */
    private void handleContextSave(ProjectNode node) {
        var model = findProjectByNodeId(node.id());
        if (model.isEmpty()) return;

        var project = model.get().project();
        var path = model.get().projectPath();

        if (path != null) {
            controller.saveProject(project, path);
            statusLabel.setText(I18n.format("status.savedTo", path.getFileName()));
        } else {
            // No path → trigger Save As for the current project
            // (switch to it first so Save As picks up the right name)
            controller.selectProject(model.get());
            handleSaveAs();
        }
    }

    /**
     * Shows a rename dialog for the project that owns the given node.
     */
    private void handleContextRename(ProjectNode node) {
        var model = findProjectByNodeId(node.id());
        if (model.isEmpty()) return;

        // Switch to this project so the rename targets it
        controller.selectProject(model.get());

        TextInputDialog dialog = new TextInputDialog(model.get().project().name());
        dialog.setTitle("重命名工程");
        dialog.setHeaderText("输入新的工程名称");
        dialog.setContentText("名称:");
        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                controller.renameProject(name.trim());
                // The workspace tree rebuilds automatically via the
                // ProjectChanged callback
                statusLabel.setText(I18n.format("status.renamed", name.trim()));
            }
        });
    }

    /**
     * Removes a parameter variable by substituting its current value
     * into all three expressions.
     */
    private void handleContextDeleteParameter(ProjectNode node) {
        String paramName = node.data();
        if (paramName == null || paramName.isBlank()) return;

        // Find and switch to the owning project first
        var model = findProjectByNodeId(node.id());
        model.ifPresent(m -> controller.selectProject(m));

        controller.removeParameter(paramName);
        statusLabel.setText(I18n.format("status.removedParam", paramName));
        statusLabel.setStyle("-fx-text-fill: #F97316; -fx-font-size: 10;");
    }

    /**
     * Extracts the project UUID from a node id and looks up
     * the corresponding ProjectTreeModel in the workspace.
     */
    private java.util.Optional<ProjectTreeModel> findProjectByNodeId(String nodeId) {
        int slash = nodeId.indexOf('/');
        String projectId = slash > 0 ? nodeId.substring(0, slash) : nodeId;
        return controller.getWorkspace().findById(projectId);
    }

    /**
     * Closes the project that owns the given node and removes it
     * from the workspace.  If it was the active project the UI resets
     * to a fresh default.
     */
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
            functionEditor.loadDefinition(controller.getCurrentDefinition());
            canvasViewport.setPoints(controller.getCurrentPoints());
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
