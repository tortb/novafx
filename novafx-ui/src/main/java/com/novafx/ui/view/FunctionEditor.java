package com.novafx.ui.view;

import com.novafx.math.FunctionDefinition;
import com.novafx.ui.editor.ExpressionEditor;
import com.novafx.ui.editor.LatexEditor;
import com.novafx.ui.editor.ModeSwitcher;
import com.novafx.ui.editor.ParserHub;
import com.novafx.ui.editor.SimpleMathEditor;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Function editor container that supports three editing modes.
 * <p>
 * Modes:
 * <ul>
 *   <li><b>简易</b> — MathSymbolPanel + expression fields (default)</li>
 *   <li><b>专业</b> — ExpressionEditor with syntax highlighting</li>
 *   <li><b>LaTeX</b> — LaTeX-style input with live preview</li>
 * </ul>
 * All modes produce {@link FunctionDefinition} through {@link ParserHub}.
 */
public final class FunctionEditor extends VBox {

    private static final Logger log = LoggerFactory.getLogger(FunctionEditor.class);

    private final ModeSwitcher modeSwitcher = new ModeSwitcher();
    private final SimpleMathEditor simpleEditor = new SimpleMathEditor();
    private final ExpressionEditor expressionEditor = new ExpressionEditor();
    private final LatexEditor latexEditor = new LatexEditor();

    private VBox activeEditor = simpleEditor;
    private Consumer<FunctionDefinition> onFunctionChanged;

    /** Creates the function editor with all three modes. */
    public FunctionEditor() {
        setStyle("-fx-background-color: #0D0D0D; -fx-border-color: #1A1A1A; -fx-border-width: 1 0 0 0;");

        // Mode switcher at top
        getChildren().add(modeSwitcher);

        // Default: show simple editor
        getChildren().add(simpleEditor);
        expressionEditor.setVisible(false);
        latexEditor.setVisible(false);

        // Mode switching
        modeSwitcher.setOnModeChanged(mode -> {
            switchEditor(mode);
        });

        // Forward changes from all editors
        var forward = (Consumer<FunctionDefinition>) def -> {
            if (onFunctionChanged != null) onFunctionChanged.accept(def);
        };

        simpleEditor.setOnFunctionChanged(forward);
        expressionEditor.setOnFunctionChanged(forward);
        latexEditor.setOnFunctionChanged(forward);
    }

    /** Sets callback when function changes. */
    public void setOnFunctionChanged(Consumer<FunctionDefinition> callback) {
        this.onFunctionChanged = callback;
    }

    /** Loads a definition into all editors. */
    public void loadDefinition(FunctionDefinition def) {
        if (def == null) return;
        simpleEditor.loadDefinition(def);
        expressionEditor.loadDefinition(def);
        latexEditor.loadDefinition(def);
    }

    /** Returns definition from the currently active editor. */
    public FunctionDefinition getDefinition() {
        return switch (modeSwitcher.getCurrentMode()) {
            case EXPRESSION -> expressionEditor.getDefinition();
            case LATEX -> latexEditor.getDefinition();
            default -> simpleEditor.getDefinition();
        };
    }

    private void switchEditor(ParserHub.InputMode mode) {
        // Hide all
        simpleEditor.setVisible(false);
        expressionEditor.setVisible(false);
        latexEditor.setVisible(false);

        // Show selected
        switch (mode) {
            case EXPRESSION -> {
                expressionEditor.setManaged(true);
                expressionEditor.setVisible(true);
                if (!getChildren().contains(expressionEditor)) {
                    getChildren().add(expressionEditor);
                }
                activeEditor = expressionEditor;
            }
            case LATEX -> {
                latexEditor.setManaged(true);
                latexEditor.setVisible(true);
                if (!getChildren().contains(latexEditor)) {
                    getChildren().add(latexEditor);
                }
                activeEditor = latexEditor;
            }
            default -> {
                simpleEditor.setManaged(true);
                simpleEditor.setVisible(true);
                if (!getChildren().contains(simpleEditor)) {
                    getChildren().add(simpleEditor);
                }
                activeEditor = simpleEditor;
            }
        }
    }
}
