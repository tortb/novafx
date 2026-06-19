package com.novafx.ui.view;

import com.novafx.math.FunctionDefinition;
import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Editable panel for the parametric function definition.
 * <p>
 * Two-column layout: X/Y/Z expressions on the left, Start/End/Step on the right.
 * Bottom progress bar shows sampling status.
 */
public final class FunctionEditor extends VBox {

    private static final Logger log = LoggerFactory.getLogger(FunctionEditor.class);

    private final TextField xField = new TextField("cos(t)");
    private final TextField yField = new TextField("sin(t)");
    private final TextField zField = new TextField("0");
    private final TextField startField = new TextField("0");
    private final TextField endField = new TextField("6.283");
    private final TextField stepField = new TextField("0.05");

    private final ProgressBar progressBar = new ProgressBar(0);
    private final Text progressText = new Text("");

    private final CompletionPopup completionPopup = new CompletionPopup();

    private Consumer<FunctionDefinition> onFunctionChanged;

    /** Creates the function editor panel. */
    public FunctionEditor() {
        setPadding(new Insets(8, 10, 8, 10));
        setSpacing(6);
        setStyle("-fx-background-color: #0D0D0D; -fx-border-color: #1A1A1A; -fx-border-width: 1 0 0 0;");

        // ── Expression fields grid ──
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(4);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPrefWidth(55);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPrefWidth(80);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);

        // Row 0: X(t)
        grid.add(label(I18n.get("editor.x")), 0, 0);
        grid.add(xField, 1, 0);
        grid.add(label(I18n.get("editor.start")), 2, 0);
        grid.add(startField, 3, 0);

        // Row 1: Y(t)
        grid.add(label(I18n.get("editor.y")), 0, 1);
        grid.add(yField, 1, 1);
        grid.add(label(I18n.get("editor.end")), 2, 1);
        grid.add(endField, 3, 1);

        // Row 2: Z(t)
        grid.add(label(I18n.get("editor.z")), 0, 2);
        grid.add(zField, 1, 2);
        grid.add(label(I18n.get("editor.step")), 2, 2);
        grid.add(stepField, 3, 2);

        // ── Progress bar row ──
        HBox progressBox = new HBox(8);
        progressBar.setPrefWidth(200);
        progressBar.setVisible(false);
        progressText.setStyle("-fx-fill: #666; -fx-font-size: 11;");
        progressBox.getChildren().addAll(progressBar, progressText);

        getChildren().addAll(grid, progressBox);

        // Wire up field events
        for (var field : new TextField[]{xField, yField, zField, startField, endField, stepField}) {
            GridPane.setHgrow(field, Priority.ALWAYS);
            field.setOnAction(e -> fireChanged());
        }

        for (var field : new TextField[]{xField, yField, zField, startField, endField, stepField}) {
            field.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (Boolean.FALSE.equals(newVal)) {
                    completionPopup.hide();
                    fireChanged();
                }
            });
        }

        // Auto-completion on expression fields
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

    /** Shows the progress bar and sets value [0..1]. */
    public void showProgress(double value, String text) {
        progressBar.setVisible(true);
        progressBar.setProgress(Math.max(0, Math.min(1, value)));
        progressText.setText(text != null ? text : "");
    }

    /** Hides the progress bar. */
    public void hideProgress() {
        progressBar.setVisible(false);
        progressText.setText("");
    }

    // ---------------------------------------------------------------
    // Auto-completion
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
            if (completionPopup.handleKeyEvent(event)) return;
            if (event.getCode() == KeyCode.TAB && completionPopup.isVisible()) {
                event.consume();
            }
            if (event.getCode() == KeyCode.ESCAPE) completionPopup.hide();
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

    private static String extractPrefix(String text, int caret) {
        if (text == null || caret <= 0) return "";
        int start = caret;
        while (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) start--;
        return text.substring(start, caret);
    }

    private static void replaceCurrentWord(TextField field, String insertText) {
        String text = field.getText();
        int caret = field.getCaretPosition();
        int start = caret;
        while (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) start--;
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
        t.setStyle("-fx-font-weight: bold; -fx-fill: #999;");
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
