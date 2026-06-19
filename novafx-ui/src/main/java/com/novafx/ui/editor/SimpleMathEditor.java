package com.novafx.ui.editor;

import com.novafx.math.FunctionDefinition;
import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * 简易数学编辑器 — 默认模式。
 * <p>
 * 布局：
 * [数学符号面板]
 * X(t) [输入框]  起始 [输入框]
 * Y(t) [输入框]  终止 [输入框]
 * Z(t) [输入框]  步长 [输入框]
 * [错误提示]
 */
public final class SimpleMathEditor extends VBox {

    private static final Logger log = LoggerFactory.getLogger(SimpleMathEditor.class);

    private final TextField xField = createField("cos(t)");
    private final TextField yField = createField("sin(t)");
    private final TextField zField = createField("0");
    private final TextField startField = createField("0");
    private final TextField endField = createField("6.283");
    private final TextField stepField = createField("0.05");

    private final MathSymbolPanel symbolPanel = new MathSymbolPanel();
    private final Text errorText = new Text();

    private Consumer<FunctionDefinition> onFunctionChanged;
    private Consumer<String> onError;

    /** 创建简易编辑器。 */
    public SimpleMathEditor() {
        setPadding(new Insets(4, 8, 4, 8));
        setSpacing(2);
        setStyle("-fx-background-color: #0D0D0D;");

        // 符号面板
        symbolPanel.setStyle("-fx-background-color: #0D0D0D; -fx-padding: 2 0;");
        getChildren().add(symbolPanel);

        // 表达式网格
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(2);

        ColumnConstraints labelCol = new ColumnConstraints(36);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        ColumnConstraints rangeLabelCol = new ColumnConstraints(28);
        ColumnConstraints rangeFieldCol = new ColumnConstraints(70);

        grid.getColumnConstraints().addAll(labelCol, fieldCol, rangeLabelCol, rangeFieldCol);

        grid.add(label("X(t)"), 0, 0);
        grid.add(xField, 1, 0);
        grid.add(label(I18n.get("editor.start")), 2, 0);
        grid.add(startField, 3, 0);

        grid.add(label("Y(t)"), 0, 1);
        grid.add(yField, 1, 1);
        grid.add(label(I18n.get("editor.end")), 2, 1);
        grid.add(endField, 3, 1);

        grid.add(label("Z(t)"), 0, 2);
        grid.add(zField, 1, 2);
        grid.add(label(I18n.get("editor.step")), 2, 2);
        grid.add(stepField, 3, 2);

        getChildren().add(grid);

        // 错误提示
        errorText.setStyle("-fx-fill: #EF4444; -fx-font-size: 11;");
        errorText.setVisible(false);
        getChildren().add(errorText);

        // ── 符号面板插入（支持 | 光标定位） ──
        symbolPanel.setOnInsert(text -> {
            TextField focused = getFocusedField();
            if (focused != null) {
                int caret = focused.getCaretPosition();
                String before = focused.getText().substring(0, caret);
                String after = focused.getText().substring(caret);

                int cursorPos = text.indexOf('|');
                String insertText;
                int newCaret;
                if (cursorPos >= 0) {
                    insertText = text.replace("|", "");
                    newCaret = caret + cursorPos;
                } else {
                    insertText = text;
                    newCaret = caret + insertText.length();
                }

                focused.setText(before + insertText + after);
                focused.positionCaret(newCaret);
                focused.requestFocus();
                fireChanged();
            }
        });

        // ── 表达式输入即时反馈 ──
        for (var field : new TextField[]{xField, yField, zField}) {
            GridPane.setHgrow(field, Priority.ALWAYS);
            field.textProperty().addListener((obs, old, nw) -> {
                if (field.isFocused()) fireChanged();
            });
            field.setOnAction(e -> fireChanged());
            field.focusedProperty().addListener((obs, old, nw) -> {
                if (Boolean.FALSE.equals(nw)) fireChanged();
            });
        }

        // 范围字段只在失去焦点时触发
        for (var field : new TextField[]{startField, endField, stepField}) {
            field.setOnAction(e -> fireChanged());
            field.focusedProperty().addListener((obs, old, nw) -> {
                if (Boolean.FALSE.equals(nw)) fireChanged();
            });
        }
    }

    public void setOnFunctionChanged(Consumer<FunctionDefinition> callback) {
        this.onFunctionChanged = callback;
    }

    /** 设置错误回调（用于显示即时错误）。 */
    public void setOnError(Consumer<String> callback) {
        this.onError = callback;
    }

    public void loadDefinition(FunctionDefinition def) {
        if (def == null) return;
        xField.setText(def.xExpression());
        yField.setText(def.yExpression());
        zField.setText(def.zExpression());
        startField.setText(String.valueOf(def.start()));
        endField.setText(String.valueOf(def.end()));
        stepField.setText(String.valueOf(def.step()));
        errorText.setVisible(false);
    }

    public FunctionDefinition getDefinition() {
        return new FunctionDefinition(
                xField.getText(), yField.getText(), zField.getText(),
                parseDouble(startField.getText(), 0),
                parseDouble(endField.getText(), 6.283),
                parseDouble(stepField.getText(), 0.05)
        );
    }

    /** 清除错误提示。 */
    public void clearError() {
        errorText.setVisible(false);
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
                var def = getDefinition();
                errorText.setVisible(false);
                if (onError != null) onError.accept(null);
                onFunctionChanged.accept(def);
            } catch (Exception e) {
                String msg = e.getMessage();
                errorText.setText("⚠ " + (msg != null ? msg : "表达式错误"));
                errorText.setVisible(true);
                if (onError != null) onError.accept(msg);
            }
        }
    }

    private static TextField createField(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.setStyle("-fx-background-color: #1A1A1A; -fx-border-color: #262626; "
                + "-fx-border-radius: 4; -fx-background-radius: 4; "
                + "-fx-text-fill: #FFF; -fx-font-family: 'JetBrains Mono', 'Consolas', monospace; "
                + "-fx-font-size: 13; -fx-padding: 6 8;");
        return tf;
    }

    private static Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #999; -fx-font-size: 12;");
        return l;
    }

    private static double parseDouble(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
