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

/**
 * P0 Simple Math Editor — default mode.
 * <p>
 * Shows a {@link MathSymbolPanel} for clicking to insert functions,
 * and three text fields for X(t), Y(t), Z(t) expressions.
 * <p>
 * Inspired by GeoGebra and Desmos.
 */
public final class SimpleMathEditor extends VBox {

    private final TextField xField = new TextField("cos(t)");
    private final TextField yField = new TextField("sin(t)");
    private final TextField zField = new TextField("0");

    private final TextField startField = new TextField("0");
    private final TextField endField = new TextField("6.283");
    private final TextField stepField = new TextField("0.05");

    private final MathSymbolPanel symbolPanel = new MathSymbolPanel();

    private Consumer<FunctionDefinition> onFunctionChanged;

    /** Creates the simple math editor. */
    public SimpleMathEditor() {
        setPadding(new Insets(4, 10, 6, 10));
        setSpacing(4);

        // Symbol panel (collapsible)
        getChildren().add(symbolPanel);

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

        grid.add(label(I18n.get("editor.x")), 0, 0);
        grid.add(xField, 1, 0);
        grid.add(label(I18n.get("editor.start")), 2, 0);
        grid.add(startField, 3, 0);

        grid.add(label(I18n.get("editor.y")), 0, 1);
        grid.add(yField, 1, 1);
        grid.add(label(I18n.get("editor.end")), 2, 1);
        grid.add(endField, 3, 1);

        grid.add(label(I18n.get("editor.z")), 0, 2);
        grid.add(zField, 1, 2);
        grid.add(label(I18n.get("editor.step")), 2, 2);
        grid.add(stepField, 3, 2);

        getChildren().add(grid);

        // Symbol panel inserts at cursor position
        symbolPanel.setOnInsert(text -> {
            TextField focused = getFocusedField();
            if (focused != null) {
                int caret = focused.getCaretPosition();
                String before = focused.getText().substring(0, caret);
                String after = focused.getText().substring(caret);
                focused.setText(before + text.replace("|", "") + after);
                focused.positionCaret(caret + text.replace("|", "").length());
                fireChanged();
            }
        });

        // Field change triggers
        for (var field : new TextField[]{xField, yField, zField, startField, endField, stepField}) {
            GridPane.setHgrow(field, Priority.ALWAYS);
            field.setOnAction(e -> fireChanged());
            field.focusedProperty().addListener((obs, old, nw) -> {
                if (Boolean.FALSE.equals(nw)) fireChanged();
            });
        }
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

    /** Returns current definition from field values. */
    public FunctionDefinition getDefinition() {
        return new FunctionDefinition(
                xField.getText(),
                yField.getText(),
                zField.getText(),
                parseDouble(startField.getText(), 0),
                parseDouble(endField.getText(), 6.283),
                parseDouble(stepField.getText(), 0.05)
        );
    }

    private TextField getFocusedField() {
        for (var f : new TextField[]{xField, yField, zField}) {
            if (f.isFocused()) return f;
        }
        return null;
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
