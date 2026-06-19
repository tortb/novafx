package com.novafx.ui.view;

import com.novafx.math.FunctionDefinition;
import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Editable panel for the parametric function definition.
 * <p>
 * Contains text fields for X(t), Y(t), Z(t) expressions and numeric
 * fields for Start, End, and Step parameters. Supports auto-completion
 * via {@link CompletionPopup}.
 */
public final class FunctionEditor extends GridPane {

    private static final Logger log = LoggerFactory.getLogger(FunctionEditor.class);

    private final TextField xField = new TextField("cos(t)");
    private final TextField yField = new TextField("sin(t)");
    private final TextField zField = new TextField("0");
    private final TextField startField = new TextField("0");
    private final TextField endField = new TextField("6.283");
    private final TextField stepField = new TextField("0.05");

    private final CompletionPopup completionPopup = new CompletionPopup();

    private Consumer<FunctionDefinition> onFunctionChanged;

    /** Creates the function editor panel. */
    public FunctionEditor() {
        setHgap(8);
        setVgap(6);
        setPadding(new Insets(10));

        addRow(0, label(I18n.get("editor.x")), xField);
        addRow(1, label(I18n.get("editor.y")), yField);
        addRow(2, label(I18n.get("editor.z")), zField);
        addRow(3, label(I18n.get("editor.start")), startField);
        addRow(4, label(I18n.get("editor.end")), endField);
        addRow(5, label(I18n.get("editor.step")), stepField);

        // Make text fields expand horizontally
        for (var field : new TextField[]{xField, yField, zField, startField, endField, stepField}) {
            GridPane.setHgrow(field, Priority.ALWAYS);
            field.setOnAction(e -> fireChanged());
        }

        // Focus loss triggers evaluation
        for (var field : new TextField[]{xField, yField, zField, startField, endField, stepField}) {
            field.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (Boolean.FALSE.equals(newVal)) {
                    completionPopup.hide();
                    fireChanged();
                }
            });
        }

        // Wire up auto-completion for expression fields
        setupCompletion(xField);
        setupCompletion(yField);
        setupCompletion(zField);
    }

    /** Sets the callback invoked when any field changes. */
    public void setOnFunctionChanged(Consumer<FunctionDefinition> callback) {
        this.onFunctionChanged = callback;
    }

    /** Loads a FunctionDefinition into the editor fields. */
    public void loadDefinition(FunctionDefinition def) {
        if (def == null) return;
        xField.setText(def.xExpression());
        yField.setText(def.yExpression());
        zField.setText(def.zExpression());
        startField.setText(String.valueOf(def.start()));
        endField.setText(String.valueOf(def.end()));
        stepField.setText(String.valueOf(def.step()));
    }

    /** Returns the current FunctionDefinition from the field values. */
    public FunctionDefinition getDefinition() {
        double start = parseDouble(startField.getText(), 0);
        double end = parseDouble(endField.getText(), 6.283);
        double step = parseDouble(stepField.getText(), 0.05);
        return new FunctionDefinition(
                xField.getText(), yField.getText(), zField.getText(),
                start, end, step
        );
    }

    // ---------------------------------------------------------------
    // Auto-completion support
    // ---------------------------------------------------------------

    private void setupCompletion(TextField field) {
        completionPopup.setOnInsert(insertText -> {
            replaceCurrentWord(field, insertText);
            fireChanged();
        });

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (field.isFocused() && newVal != null && !newVal.equals(oldVal)) {
                scheduleCompletion(field);
            }
        });

        field.setOnKeyPressed(event -> {
            if (completionPopup.handleKeyEvent(event)) {
                return;
            }

            if (event.getCode() == KeyCode.TAB && completionPopup.isVisible()) {
                event.consume();
                return;
            }

            if (event.getCode() == KeyCode.ESCAPE) {
                completionPopup.hide();
            }
        });
    }

    private void scheduleCompletion(TextField field) {
        String text = field.getText();
        int caret = field.getCaretPosition();

        String prefix = extractPrefix(text, caret);
        if (prefix.length() >= 2) {
            var window = field.getScene() != null ? field.getScene().getWindow() : null;
            if (window != null) {
                double x = field.localToScreen(field.getBoundsInLocal()).getMinX();
                double y = field.localToScreen(field.getBoundsInLocal()).getMaxY();
                completionPopup.show(window, x, y, prefix);
            }
        } else {
            completionPopup.hide();
        }
    }

    /**
     * Extracts the word being typed at the cursor position.
     * Looks backward from the caret to find the start of the current identifier.
     */
    private static String extractPrefix(String text, int caret) {
        if (text == null || caret <= 0) return "";

        int start = caret;
        while (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) {
            start--;
        }
        return text.substring(start, caret);
    }

    /**
     * Replaces the current identifier under the cursor with the completion text.
     */
    private static void replaceCurrentWord(TextField field, String insertText) {
        String text = field.getText();
        int caret = field.getCaretPosition();

        int start = caret;
        while (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) {
            start--;
        }

        String before = text.substring(0, start);
        String after = text.substring(caret);
        field.setText(before + insertText + after);
        field.positionCaret(start + insertText.length());
    }

    // ---------------------------------------------------------------
    // Event management
    // ---------------------------------------------------------------

    private void fireChanged() {
        if (onFunctionChanged != null) {
            try {
                onFunctionChanged.accept(getDefinition());
            } catch (Exception e) {
                log.warn("Invalid function expression: {}", e.getMessage());
            }
        }
    }

    private static Text label(String text) {
        Text t = new Text(text);
        t.setStyle("-fx-font-weight: bold;");
        return t;
    }

    private static double parseDouble(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
