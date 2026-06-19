package com.novafx.ui.editor;

import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A 4-tab virtual keyboard panel for mathematical expression input.
 * <p>
 * Tabs: 123 (digits), f(x) (functions), ABC (variables), #& (operators).
 * <p>
 * Each button calls {@link #onInsert} with its insert text, using the
 * {@code |} character as a cursor-position sentinel (the caller should
 * strip it and place the caret at that position).
 */
public final class MathKeyboard extends VBox {

    private Consumer<String> onInsert = s -> {};

    public MathKeyboard() {
        getStyleClass().add("math-keyboard");
        setFillWidth(true);

        TabPane tabPane = createTabPane();
        getChildren().add(tabPane);
    }

    public void setOnInsert(Consumer<String> callback) {
        this.onInsert = callback != null ? callback : s -> {};
    }

    // ── Tab pane ──

    private TabPane createTabPane() {
        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("math-keyboard-tabs");

        tabs.getTabs().addAll(
                createTab(I18n.get("keyboard.tab.digits"), buildDigitContent()),
                createTab(I18n.get("keyboard.tab.functions"), buildFunctionContent()),
                createTab(I18n.get("keyboard.tab.variables"), buildVariableContent()),
                createTab(I18n.get("keyboard.tab.operators"), buildOperatorContent())
        );

        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setPrefHeight(140);
        tabs.setMinHeight(140);
        return tabs;
    }

    private static Tab createTab(String text, Node content) {
        Tab tab = new Tab(text);
        tab.setContent(content);
        tab.setClosable(false);
        return tab;
    }

    // ── Button builders ──

    private Node buildDigitContent() {
        return buttonGrid(8, 4,
                "7", "8", "9", "(",
                "4", "5", "6", ")",
                "1", "2", "3", ",",
                "0", ".",  "π", "e"
        );
    }

    private Node buildFunctionContent() {
        // ScrollPane wrapper for functions to fit
        FlowPane pane = new FlowPane(4, 4);
        pane.setPadding(new Insets(6, 6, 6, 6));
        pane.setAlignment(Pos.CENTER);
        pane.setPrefWrapLength(480);

        for (String fn : FUNCTIONS) {
            pane.getChildren().add(createKey(fn, fn + "(|)"));
        }
        return pane;
    }

    private Node buildVariableContent() {
        return buttonGrid(6, 3,
                "t", "a", "b", "c", "d", "n",
                "x", "y", "z", "r", "m", "θ"
        );
    }

    private Node buildOperatorContent() {
        return buttonGrid(8, 3,
                "+", "-", "*", "/", "^", "%", "(", ")",
                "=", "<", ">", "!", "&", "|", ":", "?",
                "∞", "≠", "∈", "∧", "∨", "¬", "⊥", "⊗"
        );
    }

    // ── Helpers ──

    /**
     * Creates a uniform grid of buttons from a flat array of labels.
     * All buttons insert their label text directly (no cursor marker).
     */
    private TilePane buttonGrid(int columns, int rows, String... labels) {
        TilePane grid = new TilePane();
        grid.setPadding(new Insets(6, 6, 6, 6));
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setPrefColumns(columns);
        grid.setPrefRows(rows);
        grid.setAlignment(Pos.CENTER);

        for (String label : labels) {
            grid.getChildren().add(createKey(label, label));
        }
        return grid;
    }

    /**
     * Creates a single keyboard button.
     *
     * @param label      the button display text
     * @param insertText the text sent to {@link #onInsert} when clicked
     */
    private Button createKey(String label, String insertText) {
        Button btn = new Button(label);
        btn.getStyleClass().add("kb-button");
        btn.setOnAction(e -> onInsert.accept(insertText));
        return btn;
    }

    private static final List<String> FUNCTIONS = Arrays.asList(
            "sin", "cos", "tan",
            "asin", "acos", "atan",
            "sinh", "cosh", "tanh",
            "sqrt", "exp", "ln",
            "log", "abs", "floor",
            "ceil", "round"
    );
}
