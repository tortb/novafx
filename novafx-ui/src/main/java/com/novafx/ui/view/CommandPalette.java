package com.novafx.ui.view;

import com.novafx.core.workspace.ProjectNode;
import com.novafx.core.workspace.ProjectNodeType;
import com.novafx.core.workspace.ProjectTreeModel;
import com.novafx.core.workspace.Workspace;
import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Command palette overlay (Ctrl+P / Ctrl+Shift+P).
 * <p>
 * Searches across two domains simultaneously:
 * <ol>
 *   <li><strong>Commands</strong> — app-level actions (new, open, export)</li>
 *   <li><strong>Project nodes</strong> — every node in the open workspace,
 *       letting the user quickly jump to any expression, parameter,
 *       camera, or render setting by name</li>
 * </ol>
 * <p>
 * Inspired by VSCode's command palette and IntelliJ's "Search Everywhere".
 */
public final class CommandPalette {

    private final Stage stage;
    private final TextField searchField;
    private final ListView<Entry> listView;

    private final List<Entry> commandEntries = new ArrayList<>();
    private Workspace workspace;
    private Consumer<ProjectNode> onNavigateToNode;

    private record Entry(String label, String searchText, Runnable action) {
    }

    /** Creates the command palette. */
    public CommandPalette(Runnable newProject, Runnable openProject,
                          Runnable exportCsv, Runnable exportJson, Runnable exportMc) {
        // ── Static command entries ─────────────────────────────
        commandEntries.add(new Entry("📁 " + I18n.get("menu.file.new"), "new project", newProject));
        commandEntries.add(new Entry("📂 " + I18n.get("menu.file.open"), "open", openProject));
        commandEntries.add(new Entry("📄 Export CSV", "csv", exportCsv));
        commandEntries.add(new Entry("📄 Export JSON", "json", exportJson));
        commandEntries.add(new Entry("📄 Export MCFunction", "mcfunction mc", exportMc));

        // ── UI ────────────────────────────────────────────────
        this.searchField = new TextField();
        searchField.setPromptText("搜索命令 / 工程节点...");
        searchField.setStyle("-fx-font-size: 14; -fx-padding: 10;");

        this.listView = new ListView<>();
        listView.setStyle("-fx-background-color: transparent;");
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Entry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                } else {
                    setText(entry.label);
                }
                setStyle("-fx-text-fill: #CCC; -fx-padding: 8 12;");
            }
        });
        listView.setPrefHeight(300);

        VBox root = new VBox(searchField, listView);
        root.setStyle("-fx-background-color: #151515; -fx-border-color: #262626; -fx-border-width: 1;");

        Scene scene = new Scene(root, 480, 350);
        scene.setFill(null);

        this.stage = new Stage(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);

        // ── Events ──────────────────────────────────────────
        searchField.textProperty().addListener((obs, old, text) -> filter(text));

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN) {
                int idx = listView.getSelectionModel().getSelectedIndex();
                if (idx < listView.getItems().size() - 1) {
                    listView.getSelectionModel().select(idx + 1);
                }
                e.consume();
            } else if (e.getCode() == KeyCode.UP) {
                int idx = listView.getSelectionModel().getSelectedIndex();
                if (idx > 0) {
                    listView.getSelectionModel().select(idx - 1);
                }
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {
                Entry selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    close();
                    selected.action.run();
                }
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                close();
                e.consume();
            }
        });

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Entry selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    close();
                    selected.action.run();
                }
            }
        });
    }

    // ---------------------------------------------------------------
    //  Workspace binding
    // ---------------------------------------------------------------

    /**
     * Sets the workspace whose tree nodes should be searchable.
     *
     * @param workspace the current workspace
     */
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    /**
     * Sets the callback invoked when the user selects a project
     * tree node from the palette.
     *
     * @param callback receives the selected node
     */
    public void setOnNavigateToNode(Consumer<ProjectNode> callback) {
        this.onNavigateToNode = callback;
    }

    // ---------------------------------------------------------------
    //  Show / hide
    // ---------------------------------------------------------------

    /** Shows the command palette. */
    public void show() {
        stage.show();
        searchField.requestFocus();
        searchField.clear();
        filter("");
    }

    /** Hides the command palette. */
    public void close() {
        stage.hide();
    }

    // ---------------------------------------------------------------
    //  Filter
    // ---------------------------------------------------------------

    private void filter(String text) {
        List<Entry> all = new ArrayList<>(commandEntries);

        // Append workspace node entries
        if (workspace != null) {
            for (ProjectTreeModel model : workspace.getProjects()) {
                indexNodes(model.root(), "", all);
            }
        }

        if (text == null || text.isBlank()) {
            listView.getItems().setAll(all);
        } else {
            String lower = text.toLowerCase();
            listView.getItems().setAll(
                    all.stream()
                            .filter(e -> e.searchText.toLowerCase().contains(lower)
                                    || e.label.toLowerCase().contains(lower))
                            .toList()
            );
        }
        listView.getSelectionModel().selectFirst();
    }

    /**
     * Recursively collects tree nodes into the entry list,
     * building a path label like {@code "Heart / Function / x(t)"}.
     */
    private void indexNodes(ProjectNode node, String parentPath,
                            List<Entry> entries) {
        String path = parentPath.isEmpty()
                ? node.displayName()
                : parentPath + " / " + node.displayName();

        String icon = iconFor(node.nodeType());
        String label = icon + " " + path;

        // Build a rich search-text that includes type names and the raw data
        String searchText = (node.displayName() + " " + node.nodeType()
                + " " + (node.data() != null ? node.data() : "")
                + " " + path).toLowerCase();

        entries.add(new Entry(label, searchText, () -> {
            if (onNavigateToNode != null) {
                onNavigateToNode.accept(node);
            }
        }));

        for (ProjectNode child : node.children()) {
            indexNodes(child, path, entries);
        }
    }

    private static String iconFor(ProjectNodeType type) {
        return switch (type) {
            case PROJECT        -> "📁";
            case FUNCTION       -> "ƒ";
            case X_EXPR         -> "x";
            case Y_EXPR         -> "y";
            case Z_EXPR         -> "z";
            case PARAMETER_LIST -> "λ";
            case PARAMETER      -> "λ";
            case CAMERA         -> "📷";
            case RENDER         -> "🎨";
            case PRESETS        -> "◆";
            default             -> "●";
        };
    }
}
