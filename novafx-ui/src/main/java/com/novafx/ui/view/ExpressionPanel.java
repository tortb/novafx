package com.novafx.ui.view;

import com.novafx.function.CompiledFunction;
import com.novafx.function.CompletionEngine;
import com.novafx.function.CompletionItem;
import com.novafx.math.FunctionDefinition;
import com.novafx.ui.editor.MathKeyboard;
import com.novafx.ui.i18n.I18n;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.util.function.Consumer;

/**
 * Unified expression input panel that replaces the old three-mode editor
 * system (SimpleMathEditor / ExpressionEditor / LatexEditor).
 * <p>
 * Layout (top to bottom):
 * <ol>
 *   <li>Three expression rows: X(t), Y(t), Z(t) + text fields</li>
 *   <li>Range row: t ∈ [start] → [end] 步长 [step]</li>
 *   <li>Error label (hidden when no error)</li>
 *   <li>{@link MathKeyboard} (always visible)</li>
 * </ol>
 * <p>
 * Same callback API as the old {@code FunctionEditor}:
 * {@link #setOnFunctionChanged}, {@link #setOnError},
 * {@link #loadDefinition}, {@link #getDefinition},
 * {@link #focusExpression}, {@link #clearError}.
 */
public final class ExpressionPanel extends VBox {

    private final TextField xField = createField("cos(t)");
    private final TextField yField = createField("sin(t)");
    private final TextField zField = createField("0");
    private final TextField startField = new TextField("0");
    private final TextField endField = new TextField("6.283");
    private final TextField stepField = new TextField("0.05");

    private final Label errorLabel = new Label();

    private final MathKeyboard keyboard = new MathKeyboard();
    private final CompletionPopup completionPopup = new CompletionPopup();
    private final CompletionEngine completionEngine = new CompletionEngine();

    private Consumer<FunctionDefinition> onFunctionChanged = def -> {};
    private Consumer<String> onError = err -> {};

    /** Currently focused expression field (for keyboard insertion). */
    private TextField focusedField;

    public ExpressionPanel() {
        getStyleClass().add("expression-panel");
        setFillWidth(true);
        setPadding(new Insets(6, 8, 4, 8));
        setSpacing(2);

        // ── Expression rows ──
        getChildren().add(createExpressionRow(I18n.get("editor.x"), xField));
        getChildren().add(createExpressionRow(I18n.get("editor.y"), yField));
        getChildren().add(createExpressionRow(I18n.get("editor.z"), zField));

        // ── Separator ──
        Region sep = new Region();
        sep.setStyle("-fx-background-color: #1A1A1A; -fx-min-height: 1; -fx-max-height: 1;");
        sep.setPadding(new Insets(2, 0, 2, 0));
        getChildren().add(sep);

        // ── Range row ──
        getChildren().add(createRangeRow());

        // ── Error label ──
        errorLabel.getStyleClass().add("expression-error");
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 11px; -fx-padding: 2 0 0 0;");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        getChildren().add(errorLabel);

        // ── Math keyboard ──
        getChildren().add(keyboard);

        // ── Focus tracking ──
        setupFocusTracking();
        setupCompletion();

        // ── Keyboard insertion ──
        keyboard.setOnInsert(text -> insertAtFocusedField(text));

        // ── Initial validation ──
        validateAndFire();
    }

    // ── Callback API ──

    public void setOnFunctionChanged(Consumer<FunctionDefinition> callback) {
        this.onFunctionChanged = callback != null ? callback : def -> {};
    }

    public void setOnError(Consumer<String> callback) {
        this.onError = callback != null ? callback : err -> {};
    }

    // ── Data API ──

    /**
     * Loads a function definition into all fields.
     */
    public void loadDefinition(FunctionDefinition def) {
        xField.setText(def.xExpression());
        yField.setText(def.yExpression());
        zField.setText(def.zExpression());
        startField.setText(String.valueOf(def.start()));
        endField.setText(String.valueOf(def.end()));
        stepField.setText(String.valueOf(def.step()));
        clearError();
    }

    /**
     * Reads all fields and constructs a {@link FunctionDefinition}.
     * May throw if expressions are invalid.
     */
    public FunctionDefinition getDefinition() {
        double start = parseDoubleSafe(startField.getText(), 0);
        double end = parseDoubleSafe(endField.getText(), 10);
        double step = parseDoubleSafe(stepField.getText(), 0.1);
        return new FunctionDefinition(
                xField.getText(),
                yField.getText(),
                zField.getText(),
                start, end, step
        );
    }

    /**
     * Focuses and selects all text in the expression field for the given axis.
     */
    public void focusExpression(char axis) {
        TextField field = switch (axis) {
            case 'x' -> xField;
            case 'y' -> yField;
            case 'z' -> zField;
            default -> xField;
        };
        field.requestFocus();
        field.selectAll();
    }

    /**
     * Hides the error display.
     */
    public void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    // ── Layout helpers ──

    private static HBox createExpressionRow(String labelText, TextField field) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #A855F7; -fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-min-width: 48px; -fx-padding: 0 8 0 0;");
        label.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(4, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 2, 0));
        HBox.setHgrow(field, Priority.ALWAYS);
        return row;
    }

    private HBox createRangeRow() {
        Label rangeLabel = new Label(I18n.get("expression.range"));
        rangeLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px; -fx-padding: 0 4 0 0;");

        Label arrowLabel = new Label(" → ");
        arrowLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");

        Label stepLabel = new Label(I18n.get("expression.step"));
        stepLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px; -fx-padding: 0 4 0 4;");

        for (TextField f : new TextField[]{startField, endField, stepField}) {
            f.setPrefWidth(72);
            f.setMinWidth(50);
            f.setMaxWidth(80);
            f.getStyleClass().add("expression-field");
        }

        HBox row = new HBox(2, rangeLabel, startField, arrowLabel, endField, stepLabel, stepField);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 4, 0));
        return row;
    }

    // ── Focus tracking ──

    private void setupFocusTracking() {
        for (TextField field : new TextField[]{xField, yField, zField,
                startField, endField, stepField}) {
            field.focusedProperty().addListener((obs, old, focused) -> {
                if (focused) {
                    focusedField = field;
                }
            });
            // Enter → move to next field
            field.setOnAction(e -> {
                moveToNextField(field);
                validateAndFire();
            });
            // Validate on focus loss
            field.focusedProperty().addListener((obs, old, focused) -> {
                if (!focused) {
                    validateAndFire();
                }
            });
        }
    }

    private void moveToNextField(TextField current) {
        if (current == xField) yField.requestFocus();
        else if (current == yField) zField.requestFocus();
        else if (current == zField) startField.requestFocus();
        else if (current == startField) endField.requestFocus();
        else if (current == endField) stepField.requestFocus();
        else if (current == stepField) xField.requestFocus();
    }

    // ── Completion integration ──

    private void setupCompletion() {
        for (TextField field : new TextField[]{xField, yField, zField}) {
            setupCompletionFor(field);
        }
    }

    private void setupCompletionFor(TextField field) {
        // Key events for completion navigation
        field.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (completionPopup.isVisible() && completionPopup.handleKeyEvent(e)) {
                e.consume();
            }
        });

        // Text change → schedule completion
        field.textProperty().addListener((obs, old, text) -> {
            if (field.isFocused()) {
                scheduleCompletion(field);
                updateBracketMatch(field);
                validateAndFire();
            }
        });

        // Hide popup on focus loss
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) {
                Platform.runLater(completionPopup::hide);
            }
        });
    }

    private void scheduleCompletion(TextField field) {
        int caret = field.getCaretPosition();
        String text = field.getText();

        // Extract prefix: characters before caret that are letters/digits
        int start = caret;
        while (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) {
            start--;
        }
        String prefix = text.substring(start, caret);

        if (prefix.length() < 2) {
            completionPopup.hide();
            return;
        }

        // Position popup below field
        Window window = field.getScene() != null ? field.getScene().getWindow() : null;
        if (window == null) return;

        var bounds = field.localToScreen(field.getBoundsInLocal());
        completionPopup.show(window, bounds.getMinX(), bounds.getMaxY(), prefix);
    }

    /**
     * Replaces the word under the caret in the currently focused field.
     */
    private void replaceCurrentWord(TextField field, String replacement) {
        int caret = field.getCaretPosition();
        String text = field.getText();

        // Find word start (walk backward through letters/digits)
        int start = caret;
        while (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) {
            start--;
        }
        int end = caret;

        // Build new text
        String newText = text.substring(0, start) + replacement + text.substring(end);
        field.setText(newText);
        field.positionCaret(start + replacement.length());
    }

    // ── Bracket matching ──

    private void updateBracketMatch(TextField field) {
        String text = field.getText();
        int brackets = 0;
        boolean error = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') brackets++;
            else if (c == ')') {
                brackets--;
                if (brackets < 0) {
                    error = true;
                    break;
                }
            }
        }
        if (error || brackets != 0) {
            field.setStyle(field.getStyle()
                    + "-fx-border-color: #EF4444; -fx-border-width: 1.5;");
        } else {
            field.setStyle(field.getStyle()
                    .replace("-fx-border-color: #EF4444; -fx-border-width: 1.5;", ""));
        }
    }

    // ── MathKeyboard insertion ──

    private void insertAtFocusedField(String insertText) {
        TextField target = focusedField;
        if (target == null || (target != xField && target != yField && target != zField)) {
            target = xField;
        }

        int caret = target.getCaretPosition();
        String text = target.getText();

        // Handle cursor-position sentinel (|)
        int cursorPos = insertText.indexOf('|');
        String clean = insertText.replace("|", "");
        int insertOffset = cursorPos >= 0 ? cursorPos : clean.length();

        String newText = text.substring(0, caret) + clean + text.substring(caret);
        target.setText(newText);
        target.positionCaret(caret + insertOffset);
        target.requestFocus();
    }

    // ── Validation ──

    private void validateAndFire() {
        try {
            String x = xField.getText().trim();
            String y = yField.getText().trim();
            String z = zField.getText().trim();
            double start = parseDoubleSafe(startField.getText(), 0);
            double end = parseDoubleSafe(endField.getText(), 10);
            double step = parseDoubleSafe(stepField.getText(), 0.1);

            // Validate by attempting to parse
            if (!x.isEmpty()) new CompiledFunction(x);
            if (!y.isEmpty()) new CompiledFunction(y);
            if (!z.isEmpty()) new CompiledFunction(z);

            var def = new FunctionDefinition(
                    x.isEmpty() ? "0" : x,
                    y.isEmpty() ? "0" : y,
                    z.isEmpty() ? "0" : z,
                    start, end, step
            );

            clearError();
            onFunctionChanged.accept(def);

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("token")) {
                msg = I18n.get("expression.error.parseError");
            }
            showError(msg != null ? msg : I18n.get("expression.error.parseError"));
        }
    }

    private void showError(String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        onError.accept(message);
    }

    // ── Utilities ──

    private static TextField createField(String defaultValue) {
        TextField field = new TextField(defaultValue);
        field.getStyleClass().add("expression-field");
        return field;
    }

    private static double parseDoubleSafe(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
