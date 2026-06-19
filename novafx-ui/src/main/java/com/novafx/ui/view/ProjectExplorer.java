package com.novafx.ui.view;

import com.novafx.core.workspace.ProjectNode;
import com.novafx.core.workspace.ProjectNodeType;
import com.novafx.core.workspace.ProjectTreeModel;
import com.novafx.core.workspace.Workspace;
import com.novafx.ui.components.CollapsiblePanel;
import com.novafx.ui.i18n.I18n;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Left-side <strong>Project Explorer</strong> panel.
 * <p>
 * Replaces the old preset-card grid with a VSCode-style tree view
 * driven by a {@link Workspace}. Every structural element (project,
 * function, parameter, camera, render, presets) is a node in the
 * tree, automatically derived from the {@code .nfx} model rather
 * than scanned from the filesystem.
 * <p>
 * Layout:
 * <pre>
 * ┌─────────────────────────────┐
 * │  📁 Project Explorer        │  ← header
 * ├─────────────────────────────┤
 * │  ▶ Heart                    │
 * │    ├ ▶ Function             │  ← TreeView
 * │    │    x(t)                │
 * │    │    y(t)                │
 * │    │    z(t)                │
 * │    ├ · Parameters           │
 * │    ├ · Camera               │
 * │    ├ · Render               │
 * │    └ · Presets              │
 * │  ▶ DNA                      │
 * │  ▶ Spiral                   │
 * ├─────────────────────────────┤
 * │  + New Project              │  ← footer button
 * └─────────────────────────────┘
 * </pre>
 */
public final class ProjectExplorer extends CollapsiblePanel {

    private static final Map<ProjectNodeType, String> ICONS = Map.of(
            ProjectNodeType.PROJECT,        "📁",
            ProjectNodeType.FUNCTION,       "ƒ",
            ProjectNodeType.X_EXPR,         "x",
            ProjectNodeType.Y_EXPR,         "y",
            ProjectNodeType.Z_EXPR,         "z",
            ProjectNodeType.PARAMETER_LIST, "λ",
            ProjectNodeType.PARAMETER,      "λ",
            ProjectNodeType.CAMERA,         "📷",
            ProjectNodeType.RENDER,         "🎨",
            ProjectNodeType.PRESETS,        "◆"
    );

    /** 预设名称 → 显示名称（中文）映射。 */
    private static final Map<String, String> PRESET_LABELS = Map.ofEntries(
            Map.entry("Circle", "圆形"),
            Map.entry("Heart", "爱心"),
            Map.entry("Star", "星形"),
            Map.entry("Spiral", "螺旋"),
            Map.entry("DoubleSpiral", "双螺旋"),
            Map.entry("Infinity", "无穷"),
            Map.entry("Flower", "花朵"),
            Map.entry("Wave", "波浪"),
            Map.entry("Helix", "螺旋线"),
            Map.entry("DNA", "DNA"),
            Map.entry("Sphere", "球体"),
            Map.entry("Torus", "环面")
    );

    /**
     * 返回节点在当前语言下的显示名称。
     * 结构节点（Function / Parameters / …）从 I18n 取翻译，
     * 预设名（Heart / Spiral / …）用中文映射表，
     * 其余保持原样。
     */
    private static String localizedName(ProjectNode node) {
        // 结构节点 → i18n
        String i18nKey = switch (node.nodeType()) {
            case FUNCTION       -> "node.function";
            case PARAMETER_LIST -> "node.parameters";
            case CAMERA         -> "node.camera";
            case RENDER         -> "node.render";
            case PRESETS        -> "node.presets";
            case PROJECT        -> "node.project";
            default             -> null;
        };
        if (i18nKey != null) {
            return I18n.get(i18nKey);
        }
        // 预设名称 → 中文
        if (node.nodeType() == ProjectNodeType.PROJECT) {
            String label = PRESET_LABELS.get(node.displayName());
            if (label != null) return label;
        }
        // 其余保持原样（x(t), y(t), z(t), 参数名…）
        return node.displayName();
    }

    private final TreeView<ProjectNode> treeView;
    private final TreeItem<ProjectNode> invisibleRoot;
    private final Label emptyHint;

    private Workspace workspace;
    private Consumer<ProjectNode> onNodeSelected;
    private Consumer<ProjectNode> onNodeDoubleClicked;
    private Runnable onNewProject;
    private ContextMenuHandler onContextMenuAction;
    private BiConsumer<String, String> onNodeDropped;  // sourceId, targetId

    // Quick lookup: project UUID string → TreeItem
    private final Map<String, TreeItem<ProjectNode>> projectItems = new HashMap<>();

    /** Creates the Project Explorer panel. */
    public ProjectExplorer() {
        setPrefWidth(220);
        setMinWidth(180);
        setStyle("-fx-background-color: #111111; -fx-border-color: #262626; -fx-border-width: 0 1 0 0;");

        // ── Header ────────────────────────────────────────────
        Label header = new Label(I18n.get("panel.explorer"));
        header.setStyle("-fx-font-size: 12; -fx-font-weight: bold;"
                + "-fx-padding: 10 12 6 12; -fx-text-fill: #888;");

        // ── Tree ──────────────────────────────────────────────
        invisibleRoot = new TreeItem<>(null);
        invisibleRoot.setExpanded(true);

        treeView = new TreeView<>(invisibleRoot);
        treeView.setShowRoot(false);
        treeView.setId("project-tree");
        treeView.prefHeightProperty().bind(heightProperty().subtract(90));
        VBox.setVgrow(treeView, Priority.ALWAYS);

        treeView.setCellFactory(tv -> new ProjectTreeCell());

        // Selection → fire callback
        treeView.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> {
                    if (selected != null && selected.getValue() != null
                            && onNodeSelected != null) {
                        onNodeSelected.accept(selected.getValue());
                    }
                });

        // Double-click → fire callback
        treeView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                TreeItem<ProjectNode> item = treeView.getSelectionModel().getSelectedItem();
                if (item != null && item.getValue() != null
                        && onNodeDoubleClicked != null) {
                    onNodeDoubleClicked.accept(item.getValue());
                }
            }
        });

        // ── Empty-state hint ──────────────────────────────────
        emptyHint = new Label(I18n.get("panel.explorer.empty"));
        emptyHint.setStyle("-fx-text-fill: #555; -fx-font-size: 12;"
                + "-fx-padding: 24 12; -fx-alignment: center;");
        emptyHint.setWrapText(true);
        emptyHint.setMaxWidth(180);

        // ── Footer — New Project button ───────────────────────
        Button newProjectBtn = new Button("+  " + I18n.get("panel.explorer.newProject"));
        newProjectBtn.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-text-fill: #A855F7;"
                        + "-fx-font-size: 12;"
                        + "-fx-border-color: #262626;"
                        + "-fx-border-radius: 4;"
                        + "-fx-padding: 6 12;"
                        + "-fx-cursor: hand;"
                        + "-fx-min-height: 28px;"
        );
        newProjectBtn.setOnAction(e -> {
            if (onNewProject != null) onNewProject.run();
        });

        HBox footer = new HBox(newProjectBtn);
        footer.setStyle("-fx-padding: 6 8 8 8;");

        getChildren().addAll(header, treeView, emptyHint, footer);
        updateEmptyState();
    }

    // ---------------------------------------------------------------
    //  Binding
    // ---------------------------------------------------------------

    /**
     * Binds this explorer to a workspace. Rebuilds the entire tree.
     *
     * @param workspace the workspace to display; may be empty
     */
    public void setWorkspace(Workspace workspace) {
        this.workspace = Objects.requireNonNull(workspace, "workspace must not be null");
        rebuildTree();

        // React to future changes
        workspace.setOnProjectAdded(model -> rebuildTree());
        workspace.setOnProjectRemoved(this::rebuildTree);
    }

    /**
     * Selects a node by its id.
     *
     * @param nodeId the target node's {@link ProjectNode#id()}
     * @return {@code true} if found and selected
     */
    public boolean selectNode(String nodeId) {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        TreeItem<ProjectNode> found = findNode(invisibleRoot, nodeId);
        if (found != null) {
            treeView.getSelectionModel().select(found);
            scrollTo(found);
            return true;
        }
        return false;
    }

    /**
     * Expands the project root and optionally selects a sub-node.
     *
     * @param projectId the project's UUID string
     * @param subPath   optional extra path segments to select,
     *                  e.g. {@code "function/x"}
     */
    public void revealNode(String projectId, String subPath) {
        TreeItem<ProjectNode> projectItem = projectItems.get(projectId);
        if (projectItem == null) return;

        projectItem.setExpanded(true);
        if (subPath == null || subPath.isBlank()) {
            treeView.getSelectionModel().select(projectItem);
            scrollTo(projectItem);
            return;
        }

        String targetId = projectId + "/" + subPath;
        selectNode(targetId);
    }

    // ---------------------------------------------------------------
    //  Callbacks
    // ---------------------------------------------------------------

    /** Fired when a node is single-clicked (selected). */
    public void setOnNodeSelected(Consumer<ProjectNode> callback) {
        this.onNodeSelected = callback;
    }

    /** Fired when a node is double-clicked (edit intent). */
    public void setOnNodeDoubleClicked(Consumer<ProjectNode> callback) {
        this.onNodeDoubleClicked = callback;
    }

    /** Fired when the user clicks "+ New Project". */
    public void setOnNewProject(Runnable callback) {
        this.onNewProject = callback;
    }

    /**
     * Fired when a context menu action is triggered on a node.
     * Receives the target node and the action identifier string
     * (e.g. {@code "close"}, {@code "deleteParameter"},
     * {@code "copyExpression"}, {@code "addParameter"}).
     */
    public void setOnContextMenuAction(ContextMenuHandler callback) {
        this.onContextMenuAction = callback;
    }

    /**
     * Fired when a node is dragged and dropped onto another node.
     * Receives (sourceNodeId, targetNodeId).
     */
    public void setOnNodeDropped(BiConsumer<String, String> callback) {
        this.onNodeDropped = callback;
    }

    /**
     * Handler for context menu actions.
     *
     * @param node   the tree node the action was triggered on
     * @param action short string identifying the action
     */
    @FunctionalInterface
    public interface ContextMenuHandler {
        void handle(ProjectNode node, String action);
    }

    // ---------------------------------------------------------------
    //  Internal
    // ---------------------------------------------------------------

    private void rebuildTree() {
        invisibleRoot.getChildren().clear();
        projectItems.clear();

        if (workspace == null || workspace.isEmpty()) {
            updateEmptyState();
            return;
        }

        for (ProjectTreeModel model : workspace.getProjects()) {
            TreeItem<ProjectNode> projectItem = buildItem(model.root());
            projectItem.setExpanded(true);
            invisibleRoot.getChildren().add(projectItem);

            String uuid = model.project().id().toString();
            projectItems.put(uuid, projectItem);
        }

        updateEmptyState();
    }

    private TreeItem<ProjectNode> buildItem(ProjectNode node) {
        TreeItem<ProjectNode> item = new TreeItem<>(node);

        // Expand containers by default: project root + function
        if (node.nodeType().isContainer()) {
            item.setExpanded(node.nodeType() == ProjectNodeType.PROJECT);
        }

        for (ProjectNode child : node.children()) {
            item.getChildren().add(buildItem(child));
        }

        return item;
    }

    private void updateEmptyState() {
        boolean empty = workspace == null || workspace.isEmpty();
        treeView.setVisible(!empty);
        treeView.setManaged(!empty);
        emptyHint.setVisible(empty);
        emptyHint.setManaged(empty);
    }

    private static TreeItem<ProjectNode> findNode(TreeItem<ProjectNode> root,
                                                   String targetId) {
        if (root.getValue() != null && targetId.equals(root.getValue().id())) {
            return root;
        }
        for (TreeItem<ProjectNode> child : root.getChildren()) {
            TreeItem<ProjectNode> found = findNode(child, targetId);
            if (found != null) return found;
        }
        return null;
    }

    private void scrollTo(TreeItem<ProjectNode> item) {
        int row = treeView.getRow(item);
        if (row >= 0) {
            treeView.scrollTo(row);
        }
    }

    // ---------------------------------------------------------------
    //  Custom cell
    // ---------------------------------------------------------------

    /**
     * Renders a single tree cell with icon prefix, context menu,
     * and proper hover/selection styling (32px row height).
     * <p>
     * Right-click context menu differs per {@link ProjectNodeType}:
     * <ul>
     *   <li>PROJECT — Close, Save</li>
     *   <li>FUNCTION — (expand/collapse only)</li>
     *   <li>X/Y/Z_EXPR — Copy Expression</li>
     *   <li>PARAMETER_LIST — Add Parameter</li>
     *   <li>PARAMETER — Edit, Delete</li>
     *   <li>PRESETS — Browse</li>
     * </ul>
     */
    private final class ProjectTreeCell extends TreeCell<ProjectNode> {

        private final Label iconLabel = new Label();
        private final Label nameLabel = new Label();
        private final HBox container = new HBox(6);

        ProjectTreeCell() {
            container.getChildren().addAll(iconLabel, nameLabel);
            getStyleClass().add("project-tree-cell");

            // ── Drag source ───────────────────────────────────
            setOnDragDetected(e -> {
                ProjectNode item = getItem();
                if (item == null) return;
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(item.id());
                db.setContent(content);
                e.consume();
            });

            // ── Drag target ───────────────────────────────────
            setOnDragOver(e -> {
                if (e.getGestureSource() != this
                        && e.getDragboard().hasString()
                        && getItem() != null) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
                e.consume();
            });

            // ── Drop ──────────────────────────────────────────
            setOnDragDropped(e -> {
                Dragboard db = e.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    String sourceId = db.getString();
                    ProjectNode target = getItem();
                    if (target != null && onNodeDropped != null) {
                        onNodeDropped.accept(sourceId, target.id());
                        success = true;
                    }
                }
                e.setDropCompleted(success);
                e.consume();
            });
        }

        @Override
        protected void updateItem(ProjectNode node, boolean empty) {
            super.updateItem(node, empty);

            if (empty || node == null) {
                setGraphic(null);
                setText(null);
                setContextMenu(null);
                return;
            }

            iconLabel.setText(iconFor(node.nodeType()));
            iconLabel.setStyle("-fx-text-fill: #A855F7; -fx-font-size: 14;");

            nameLabel.setText(localizedName(node));
            nameLabel.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 14;");

            setTooltip(new Tooltip(node.id()));
            setGraphic(container);
            setText(null);

            setContextMenu(buildContextMenu(node));
        }

        private ContextMenu buildContextMenu(ProjectNode node) {
            var menu = new ContextMenu();

            switch (node.nodeType()) {
                case PROJECT -> {
                    menu.getItems().add(menuItem(I18n.get("explorer.menu.save"),      node, "save"));
                    menu.getItems().add(menuItem(I18n.get("explorer.menu.close"),     node, "close"));
                    menu.getItems().add(new SeparatorMenuItem());
                    menu.getItems().add(menuItem(I18n.get("explorer.menu.rename"), node, "rename"));
                }
                case FUNCTION, PARAMETER_LIST -> {
                    // only expand/collapse — no actionable items
                }
                case X_EXPR, Y_EXPR, Z_EXPR -> {
                    menu.getItems().add(menuItem(I18n.get("explorer.menu.copyExpr"), node, "copyExpr"));
                }
                case PARAMETER -> {
                    menu.getItems().add(menuItem(I18n.get("explorer.menu.edit"),   node, "editParameter"));
                    menu.getItems().add(new SeparatorMenuItem());
                    menu.getItems().add(menuItem(I18n.get("explorer.menu.delete"), node, "deleteParameter"));
                }
                case CAMERA -> {
                    menu.getItems().add(menuItem(I18n.get("explorer.menu.settings"), node, "cameraSettings"));
                }
                case RENDER -> {
                    menu.getItems().add(menuItem(I18n.get("explorer.menu.settings"), node, "renderSettings"));
                }
                case PRESETS -> {
                    menu.getItems().add(menuItem(I18n.get("explorer.menu.browsePresets"), node, "browsePresets"));
                }
                default -> {}
            }

            return menu.getItems().isEmpty() ? null : menu;
        }

        private MenuItem menuItem(String label, ProjectNode node, String action) {
            var item = new MenuItem(label);
            item.setOnAction(e -> {
                if (onContextMenuAction != null) {
                    onContextMenuAction.handle(node, action);
                }
            });
            return item;
        }

        private static String iconFor(ProjectNodeType type) {
            return ICONS.getOrDefault(type, "●");
        }
    }
}
