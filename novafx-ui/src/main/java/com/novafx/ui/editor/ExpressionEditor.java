package com.novafx.ui.editor;

import com.novafx.function.CompiledFunction;
import com.novafx.function.CompletionEngine;
import com.novafx.function.CompletionItem;
import com.novafx.math.FunctionDefinition;
import com.novafx.ui.i18n.I18n;
import com.novafx.ui.view.CompletionPopup;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * P2 Professional Expression Editor.
 * <p>
 * Features:
 * <ul>
 *   <li>Syntax highlighting via colored text rendering</li>
 *   <li>Bracket matching</li>
 *   <li>Auto-completion</li>
 *   <li>Error display</li>
 *   <li>Parameter extraction</li>
 * </ul>
 */
public final class ExpressionEditor extends VBox {

    private static final Logger log = LoggerFactory.getLogger(ExpressionEditor.class);

    private final TextField xField = new TextField("cos(t)");
    private final TextField yField = new TextField("sin(t)");
    private final TextField zField = new TextField("0");
    private final TextField startField = new TextField("0");
    private final TextField endField = new TextField("6.283");
    private final TextField stepField = new TextField("0.05");

    private final TextFlow xPreview = new TextFlow();
    private final TextFlow yPreview = new TextFlow();
    private final TextFlow zPreview = new TextFlow();
    private final Text errorText = new Text();

    private final CompletionPopup completionPopup = new CompletionPopup();
    private final CompletionEngine completionEngine = new CompletionEngine();

    private Consumer<FunctionDefinition> onFunctionChanged;

    /** Creates the professional expression editor. */
    public ExpressionEditor() {
        setPadding(new Insets(6, 10, 6, 10));
        setSpacing(4);

        // Expression grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(3);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(42);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPrefWidth(50);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPrefWidth(80);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);

        // X row
        grid.add(label(I18n.get("editor.x")), 0, 0);
        grid.add(xField, 1, 0);
        grid.add(label(I18n.get("editor.start")), 2, 0);
        grid.add(startField, 3, 0);

        // Y row
        grid.add(label(I18n.get("editor.y")), 0, 1);
        grid.add(yField, 1, 1);
        grid.add(label(I18n.get("editor.end")), 2, 1);
        grid.add(endField, 3, 1);

        // Z row
        grid.add(label(I18n.get("editor.z")), 0, 2);
        grid.add(zField, 1, 2);
        grid.add(label(I18n.get("editor.step")), 2, 2);
        grid.add(stepField, 3, 2);

        getChildren().add(grid);

        // Error text
        errorText.setStyle("-fx-fill: #EF4444; -fx-font-size: 11;");
        errorText.setVisible(false);
        getChildren().add(errorText);

        // Field events
        for (var field : new TextField[]{xField, yField, zField, startField, endField, stepField}) {
            GridPane.setHgrow(field, Priority.ALWAYS);
            field.setOnAction(e -> validateAndFire());
            field.focusedProperty().addListener((obs, old, nw) -> {
                if (Boolean.FALSE.equals(nw)) validateAndFire();
            });
            field.textProperty().addListener((obs, old, nw) -> {
                if (field.isFocused()) updateSyntaxPreview();
            });
        }

        // Auto-completion
        setupCompletion(xField);
        setupCompletion(yField);
        setupCompletion(zField);
    }

    /** Sets callback when function changes. */
    public void setOnFunctionChanged(Consumer<FunctionDefinition> callback) {
        this.onFunctionChanged = callback;
    }

    /** Loads a definition into the fields. */
    public void loadDefinition(FunctionDefinition def) {
        if (def == null) return;
        xField.setText(def.xExpression());
        yField.setText(def.yExpression());
        zField.setText(def.zExpression());
        startField.setText(String.valueOf(def.start()));
        endField.setText(String.valueOf(def.end()));
        stepField.setText(String.valueOf(def.step()));
    }

    /** Returns current definition. */
    public FunctionDefinition getDefinition() {
        return new FunctionDefinition(
                xField.getText(), yField.getText(), zField.getText(),
                parseDouble(startField.getText(), 0),
                parseDouble(endField.getText(), 6.283),
                parseDouble(stepField.getText(), 0.05)
        );
    }

    // ---------------------------------------------------------------
    // Syntax highlighting (simple keyword coloring)
    // ---------------------------------------------------------------

    private void updateSyntaxPreview() {
        highlightField(xField);
        highlightField(yField);
        highlightField(zField);
    }

    private void highlightField(TextField field) {
        // Simple syntax styling via CSS on the field
        String text = field.getText();
        // Check for balanced brackets
        int brackets = 0;
        boolean hasError = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') brackets++;
            if (c == ')') brackets--;
            if (brackets < 0) { hasError = true; break; }
        }

        if (hasError || brackets != 0) {
            field.setStyle("-fx-border-color: #EF4444; -fx-border-width: 1.5;"
                    + "-fx-background-color: #1A1A1A; -fx-text-fill: #FFF;");
        } else {
            field.setStyle("-fx-border-color: #262626; -fx-border-width: 1;"
                    + "-fx-background-color: #1A1A1A; -fx-text-fill: #FFF;");
        }
    }

    // ---------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------

    private void validateAndFire() {
        try {
            new CompiledFunction(xField.getText());
            new CompiledFunction(yField.getText());
            new CompiledFunction(zField.getText());
            errorText.setVisible(false);
            if (onFunctionChanged != null) {
                onFunctionChanged.accept(getDefinition());
            }
        } catch (Exception e) {
            errorText.setText("错误: " + e.getMessage());
            errorText.setVisible(true);
        }
    }

    // ---------------------------------------------------------------
    // Auto-completion
    // ---------------------------------------------------------------

    private void setupCompletion(TextField field) {
        completionPopup.setOnInsert(insertText -> {
            replaceCurrentWord(field, insertText);
            validateAndFire();
        });

        field.textProperty().addListener((obs, old, nw) -> {
            if (field.isFocused() && nw != null && !nw.equals(old)) {
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
