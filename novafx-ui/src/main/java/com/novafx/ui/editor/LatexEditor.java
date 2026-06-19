package com.novafx.ui.editor;

import com.novafx.math.FunctionDefinition;
import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * P3 LaTeX Editor mode.
 * <p>
 * Accepts LaTeX-style math expressions and converts them to standard
 * expression syntax for the AST pipeline. Supports basic LaTeX
 * commands like {@code \sin}, {@code \cos}, {@code \sqrt}, etc.
 */
public final class LatexEditor extends VBox {

    private final TextField xField = new TextField("\\cos(t)");
    private final TextField yField = new TextField("\\sin(t)");
    private final TextField zField = new TextField("0");
    private final TextField startField = new TextField("0");
    private final TextField endField = new TextField("6.283");
    private final TextField stepField = new TextField("0.05");

    private final Text xPreview = new Text();
    private final Text yPreview = new Text();
    private final Text zPreview = new Text();

    private Consumer<FunctionDefinition> onFunctionChanged;

    private static final Pattern LATEX_CMD = Pattern.compile("\\\\([a-zA-Z]+)");

    /** Creates the LaTeX editor. */
    public LatexEditor() {
        setPadding(new Insets(6, 10, 6, 10));
        setSpacing(4);

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

        // X(t) row with preview
        grid.add(label("X(t)"), 0, 0);
        grid.add(xField, 1, 0);
        grid.add(label(I18n.get("editor.start")), 2, 0);
        grid.add(startField, 3, 0);
        grid.add(xPreview, 1, 1);

        grid.add(label("Y(t)"), 0, 2);
        grid.add(yField, 1, 2);
        grid.add(label(I18n.get("editor.end")), 2, 2);
        grid.add(endField, 3, 2);
        grid.add(yPreview, 1, 3);

        grid.add(label("Z(t)"), 0, 4);
        grid.add(zField, 1, 4);
        grid.add(label(I18n.get("editor.step")), 2, 4);
        grid.add(stepField, 3, 4);
        grid.add(zPreview, 1, 5);

        getChildren().add(grid);

        // Preview styling
        for (var p : new Text[]{xPreview, yPreview, zPreview}) {
            p.setStyle("-fx-fill: #666; -fx-font-size: 11; -fx-font-style: italic;");
        }

        // Field events
        for (var field : new TextField[]{xField, yField, zField, startField, endField, stepField}) {
            GridPane.setHgrow(field, Priority.ALWAYS);
            field.setOnAction(e -> fireChanged());
            field.focusedProperty().addListener((obs, old, nw) -> {
                if (Boolean.FALSE.equals(nw)) fireChanged();
            });
            field.textProperty().addListener((obs, old, nw) -> updatePreviews());
        }

        updatePreviews();
    }

    /** Sets callback when function changes. */
    public void setOnFunctionChanged(Consumer<FunctionDefinition> callback) {
        this.onFunctionChanged = callback;
    }

    /** Loads definition (converts to LaTeX convention). */
    public void loadDefinition(FunctionDefinition def) {
        if (def == null) return;
        xField.setText(toLatex(def.xExpression()));
        yField.setText(toLatex(def.yExpression()));
        zField.setText(toLatex(def.zExpression()));
        startField.setText(String.valueOf(def.start()));
        endField.setText(String.valueOf(def.end()));
        stepField.setText(String.valueOf(def.step()));
    }

    /** Returns definition (converts from LaTeX). */
    public FunctionDefinition getDefinition() {
        return new FunctionDefinition(
                fromLatex(xField.getText()),
                fromLatex(yField.getText()),
                fromLatex(zField.getText()),
                parseDouble(startField.getText(), 0),
                parseDouble(endField.getText(), 6.283),
                parseDouble(stepField.getText(), 0.05)
        );
    }

    // ---------------------------------------------------------------
    // LaTeX ↔ Expression conversion
    // ---------------------------------------------------------------

    /** Converts LaTeX command syntax to standard expression syntax. */
    static String fromLatex(String latex) {
        if (latex == null || latex.isBlank()) return latex;
        String result = latex;
        // \frac{a}{b} → (a)/(b) — must be processed BEFORE generic command removal
        result = result.replaceAll("\\\\frac\\{([^}]*)\\}\\{([^}]*)\\}", "($1)/($2)");
        // \sqrt{x} → sqrt(x)
        result = result.replaceAll("\\\\sqrt\\{([^}]*)\\}", "sqrt($1)");
        // \sin → sin, \cos → cos, etc. — generic command removal
        result = LATEX_CMD.matcher(result).replaceAll(mr -> mr.group(1));
        // Remove remaining braces
        result = result.replace("{", "").replace("}", "");
        return result;
    }

    /** Converts expression to LaTeX-style (add backslashes to functions). */
    static String toLatex(String expr) {
        if (expr == null || expr.isBlank()) return expr;
        // Common function names to prefix with backslash
        String[] funcs = {"sin", "cos", "tan", "sqrt", "log", "exp", "abs", "floor", "ceil"};
        String result = expr;
        for (String f : funcs) {
            result = result.replaceAll("(?<!\\\\)" + f + "\\(", "\\\\" + f + "(");
        }
        return result;
    }

    private void updatePreviews() {
        xPreview.setText("→ " + fromLatex(xField.getText()));
        yPreview.setText("→ " + fromLatex(yField.getText()));
        zPreview.setText("→ " + fromLatex(zField.getText()));
    }

    private void fireChanged() {
        if (onFunctionChanged != null) {
            try {
                onFunctionChanged.accept(getDefinition());
            } catch (Exception e) {
                // ignore parse errors during editing
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
