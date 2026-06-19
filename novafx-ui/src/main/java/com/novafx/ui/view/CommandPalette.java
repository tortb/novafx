package com.novafx.ui.view;

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

import java.util.List;
import java.util.function.Consumer;

/**
 * Command palette overlay (Ctrl+Shift+P).
 * <p>
 * Shows a searchable list of commands. Inspired by VSCode's command palette.
 */
public final class CommandPalette {

    private final Stage stage;
    private final TextField searchField;
    private final ListView<Command> listView;
    private final List<Command> commands;

    private record Command(String label, Runnable action) {
    }

    /** Creates the command palette. */
    public CommandPalette(Runnable newProject, Runnable openProject,
                          Runnable exportCsv, Runnable exportJson, Runnable exportMc) {
        this.commands = List.of(
                new Command("新建项目", newProject),
                new Command("打开项目", openProject),
                new Command("导出 CSV", exportCsv),
                new Command("导出 JSON", exportJson),
                new Command("导出 MCFunction", exportMc)
        );

        this.searchField = new TextField();
        searchField.setPromptText("输入命令...");
        searchField.setStyle("-fx-font-size: 14; -fx-padding: 10;");

        this.listView = new ListView<>();
        listView.setStyle("-fx-background-color: transparent;");
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Command cmd, boolean empty) {
                super.updateItem(cmd, empty);
                setText(empty || cmd == null ? null : cmd.label);
                setStyle("-fx-text-fill: #CCC; -fx-padding: 8 12;");
            }
        });
        listView.setPrefHeight(200);

        VBox root = new VBox(searchField, listView);
        root.setStyle("-fx-background-color: #151515; -fx-border-color: #262626; -fx-border-width: 1;");

        Scene scene = new Scene(root, 400, 250);
        scene.setFill(null);

        this.stage = new Stage(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);

        // Search filtering
        searchField.textProperty().addListener((obs, old, text) -> filter(text));

        // Navigation
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
                Command selected = listView.getSelectionModel().getSelectedItem();
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

        // Click to select
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Command selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    close();
                    selected.action.run();
                }
            }
        });

        filter("");
    }

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

    private void filter(String text) {
        if (text == null || text.isBlank()) {
            listView.getItems().setAll(commands);
        } else {
            String lower = text.toLowerCase();
            listView.getItems().setAll(
                    commands.stream()
                            .filter(c -> c.label.toLowerCase().contains(lower))
                            .toList()
            );
        }
        listView.getSelectionModel().selectFirst();
    }
}
