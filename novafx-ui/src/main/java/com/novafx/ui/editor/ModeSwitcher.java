package com.novafx.ui.editor;

import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

/**
 * Three-way mode switcher for the function editor.
 * <p>
 * Toggles between: [简易] [专业] [LaTeX]
 * <p>
 * Styled to match Nova Dark theme with Nova Orange accent.
 */
public final class ModeSwitcher extends HBox {

    private final ToggleGroup group = new ToggleGroup();
    private Consumer<ParserHub.InputMode> onModeChanged;

    private static final String BASE_STYLE =
            "-fx-background-color: #111111;"
                    + "-fx-border-color: #262626;"
                    + "-fx-border-radius: 4;"
                    + "-fx-background-radius: 4;"
                    + "-fx-text-fill: #666;"
                    + "-fx-font-size: 12;"
                    + "-fx-padding: 4 14;"
                    + "-fx-cursor: hand;";

    private static final String SELECTED_STYLE =
            "-fx-background-color: #F97316;"
                    + "-fx-border-color: #F97316;"
                    + "-fx-border-radius: 4;"
                    + "-fx-background-radius: 4;"
                    + "-fx-text-fill: #FFFFFF;"
                    + "-fx-font-size: 12;"
                    + "-fx-font-weight: bold;"
                    + "-fx-padding: 4 14;"
                    + "-fx-cursor: hand;";

    /** Creates the mode switcher. */
    public ModeSwitcher() {
        setSpacing(0);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-padding: 4 0;");

        ToggleButton simpleBtn = createButton("简易", ParserHub.InputMode.SIMPLE);
        ToggleButton exprBtn = createButton("专业", ParserHub.InputMode.EXPRESSION);
        ToggleButton latexBtn = createButton("LaTeX", ParserHub.InputMode.LATEX);

        simpleBtn.setSelected(true);

        getChildren().addAll(simpleBtn, exprBtn, latexBtn);
    }

    /** Sets callback for mode changes. */
    public void setOnModeChanged(Consumer<ParserHub.InputMode> callback) {
        this.onModeChanged = callback;
    }

    /** Returns the currently selected mode. */
    public ParserHub.InputMode getCurrentMode() {
        ToggleButton selected = (ToggleButton) group.getSelectedToggle();
        if (selected == null) return ParserHub.InputMode.SIMPLE;
        return (ParserHub.InputMode) selected.getUserData();
    }

    private ToggleButton createButton(String text, ParserHub.InputMode mode) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setUserData(mode);
        btn.setStyle(BASE_STYLE);

        btn.selectedProperty().addListener((obs, old, selected) -> {
            btn.setStyle(selected ? SELECTED_STYLE : BASE_STYLE);
            if (selected && onModeChanged != null) {
                onModeChanged.accept(mode);
            }
        });

        return btn;
    }
}
