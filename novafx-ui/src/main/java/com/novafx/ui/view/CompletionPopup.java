package com.novafx.ui.view;

import com.novafx.function.CompletionEngine;
import com.novafx.function.CompletionKind;
import com.novafx.function.CompletionItem;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.util.List;
import java.util.function.Consumer;

/**
 * An auto-completion popup that appears below a text field during editing.
 * <p>
 * Shows function names, variables, constants, and snippet suggestions
 * as the user types. Supports keyboard navigation (↑↓, Tab/Enter, Esc).
 */
public final class CompletionPopup {

    private final Popup popup;
    private final ListView<CompletionItem> listView;
    private final CompletionEngine engine;

    private Consumer<String> onInsert;

    /** Creates a completion popup. */
    public CompletionPopup() {
        this.engine = new CompletionEngine();
        this.listView = new ListView<>();
        this.popup = new Popup();

        listView.setPrefWidth(280);
        listView.setPrefHeight(200);
        listView.setCellFactory(lv -> new CompletionCell());
        listView.setStyle(
                "-fx-background-color: #1a1a1a;"
                        + "-fx-border-color: #333;"
                        + "-fx-border-width: 1;"
                        + "-fx-font-size: 13;"
        );

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                insertSelected();
            }
        });

        listView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.TAB) {
                insertSelected();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                hide();
                e.consume();
            }
        });

        popup.getContent().add(listView);
        popup.setAutoHide(true);
    }

    /**
     * Registers a callback invoked when a completion is accepted.
     *
     * @param callback receives the insertText of the selected item
     */
    public void setOnInsert(Consumer<String> callback) {
        this.onInsert = callback;
    }

    /**
     * Shows the popup below the given window at the specified position.
     *
     * @param owner the parent window
     * @param anchorX x position in screen coordinates
     * @param anchorY y position in screen coordinates
     * @param prefix the current text prefix to filter completions
     */
    public void show(Window owner, double anchorX, double anchorY, String prefix) {
        List<CompletionItem> suggestions = engine.suggest(prefix);
        if (suggestions.isEmpty()) {
            hide();
            return;
        }

        listView.getItems().setAll(suggestions);
        listView.getSelectionModel().selectFirst();

        if (!popup.isShowing()) {
            popup.show(owner, anchorX, anchorY);
        }
    }

    /** Hides the popup. */
    public void hide() {
        popup.hide();
    }

    /** Returns true if the popup is currently visible. */
    public boolean isVisible() {
        return popup.isShowing();
    }

    /**
     * Handles key events for navigation. Returns true if the event was consumed.
     *
     * @param event the key event
     * @return true if the popup handled the event
     */
    public boolean handleKeyEvent(KeyEvent event) {
        if (!isVisible()) return false;

        if (event.getCode() == KeyCode.DOWN) {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx < listView.getItems().size() - 1) {
                listView.getSelectionModel().select(idx + 1);
                listView.scrollTo(idx + 1);
            }
            event.consume();
            return true;
        }

        if (event.getCode() == KeyCode.UP) {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx > 0) {
                listView.getSelectionModel().select(idx - 1);
                listView.scrollTo(idx - 1);
            }
            event.consume();
            return true;
        }

        return false;
    }

    private void insertSelected() {
        CompletionItem selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null && onInsert != null) {
            onInsert.accept(selected.insertText());
        }
        hide();
    }

    // ---------------------------------------------------------------
    // Cell rendering
    // ---------------------------------------------------------------

    private static final class CompletionCell extends ListCell<CompletionItem> {
        @Override
        protected void updateItem(CompletionItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label kindLabel = new Label(kindTag(item.kind()));
            kindLabel.setStyle(kindStyle(item.kind()));

            Text nameText = new Text(item.label());
            nameText.setStyle("-fx-fill: #eee; -fx-font-weight: bold;");

            Text detailText = new Text("  " + item.detail());
            detailText.setStyle("-fx-fill: #888;");

            TextFlow flow = new TextFlow(kindLabel, nameText, detailText);
            flow.setPrefWidth(260);

            setGraphic(flow);
            setStyle("-fx-background-color: transparent;");
            setPadding(new Insets(4, 8, 4, 8));
        }

        private static String kindTag(CompletionKind kind) {
            return switch (kind) {
                case FUNCTION -> "fn ";
                case VARIABLE -> "var ";
                case CONSTANT -> "cst ";
                case SNIPPET -> "snip ";
            };
        }

        private static String kindStyle(CompletionKind kind) {
            String color = switch (kind) {
                case FUNCTION -> "#569CD6";
                case VARIABLE -> "#4EC9B0";
                case CONSTANT -> "#CE9178";
                case SNIPPET -> "#C586C0";
            };
            return "-fx-text-fill: " + color + "; -fx-font-size: 11;";
        }
    }
}
