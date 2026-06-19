package com.novafx.ui.editor;

import com.novafx.function.CompiledFunction;
import com.novafx.function.CompletionEngine;
import com.novafx.function.CompletionItem;
import com.novafx.math.FunctionDefinition;
import com.novafx.ui.i18n.I18n;
import com.novafx.ui.view.CompletionPopup;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * 专业表达式编辑器 — 紧凑代码编辑器风格。
 * <p>
 * 参考 VSCode / JetBrains 布局：
 * <pre>
 * X(t)  [══════════════════════════]  起始 [══════]
 * Y(t)  [══════════════════════════]  终止 [══════]
 * Z(t)  [══════════════════════════]  步长 [══════]
 * </pre>
 * 每行高度 80px，自动补全，语法高亮，即时错误提示。
 */
public final class ExpressionEditor extends VBox {

    private static final Logger log = LoggerFactory.getLogger(ExpressionEditor.class);

    private final TextField xField = createCodeField("cos(t)");
    private final TextField yField = createCodeField("sin(t)");
    private final TextField zField = createCodeField("0");
    private final TextField startField = createRangeField("0");
    private final TextField endField = createRangeField("6.283");
    private final TextField stepField = createRangeField("0.05");

    private final Text errorText = new Text();

    private final CompletionPopup completionPopup = new CompletionPopup();
    private final CompletionEngine completionEngine = new CompletionEngine();

    private Consumer<FunctionDefinition> onFunctionChanged;
    private Consumer<String> onError;

    /** 创建专业编辑器。 */
    public ExpressionEditor() {
        setPadding(new Insets(6, 8, 6, 8));
        setSpacing(4);
        setStyle("-fx-background-color: #0D0D0D;");

        // 三行表达式（紧凑编码器风格）
        VBox exprRows = new VBox(0);
        exprRows.getChildren().addAll(
                createExpressionRow("X(t)", xField, I18n.get("editor.start"), startField),
                createExpressionRow("Y(t)", yField, I18n.get("editor.end"), endField),
                createExpressionRow("Z(t)", zField, I18n.get("editor.step"), stepField)
        );

        getChildren().add(exprRows);

        // 错误提示
        errorText.setStyle("-fx-fill: #EF4444; -fx-font-size: 11; -fx-padding: 2 0 0 4;");
        errorText.setVisible(false);
        getChildren().add(errorText);

        // 字段事件
        for (var field : new TextField[]{xField, yField, zField}) {
            field.textProperty().addListener((obs, old, nw) -> {
                if (field.isFocused()) {
                    updateHighlight(field);
                    scheduleCompletion(field);
                    validateAndFire();
                }
            });
        }

        // 范围字段只触发确认操作
        for (var field : new TextField[]{startField, endField, stepField}) {
            field.setOnAction(e -> validateAndFire());
            field.focusedProperty().addListener((obs, old, nw) -> {
                if (Boolean.FALSE.equals(nw)) validateAndFire();
            });
        }

        // 自动补全
        setupCompletion(xField);
        setupCompletion(yField);
        setupCompletion(zField);
    }

    public void setOnFunctionChanged(Consumer<FunctionDefinition> callback) {
        this.onFunctionChanged = callback;
    }

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
        updateHighlight(xField);
        updateHighlight(yField);
        updateHighlight(zField);
    }

    public FunctionDefinition getDefinition() {
        return new FunctionDefinition(
                xField.getText(), yField.getText(), zField.getText(),
                parseDouble(startField.getText(), 0),
                parseDouble(endField.getText(), 6.283),
                parseDouble(stepField.getText(), 0.05)
        );
    }

    public void clearError() {
        errorText.setVisible(false);
    }

    // ---------------------------------------------------------------
    // 布局
    // ---------------------------------------------------------------

    /** 创建一行表达式：标签 [代码输入框] 范围标签 [范围输入框] */
    private HBox createExpressionRow(String label, TextField codeField,
                                     String rangeLabel, TextField rangeField) {
        HBox row = new HBox(6);
        row.setPadding(new Insets(2, 0, 2, 0));
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #F97316; "
                + "-fx-font-size: 13; -fx-min-width: 32;");

        // 代码输入框水平扩展
        HBox.setHgrow(codeField, Priority.ALWAYS);
        codeField.setPrefHeight(32);
        codeField.setMinHeight(32);
        codeField.setMaxHeight(32);

        Label rangeLbl = new Label(rangeLabel);
        rangeLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #666; -fx-min-width: 24;");

        rangeField.setPrefWidth(72);
        rangeField.setPrefHeight(32);
        rangeField.setMinHeight(32);

        row.getChildren().addAll(lbl, codeField, rangeLbl, rangeField);
        row.setPrefHeight(36);
        return row;
    }

    // ---------------------------------------------------------------
    // 语法高亮（括号匹配 + 错误边框）
    // ---------------------------------------------------------------

    private void updateHighlight(TextField field) {
        String text = field.getText();
        int brackets = 0;
        boolean hasError = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') brackets++;
            if (c == ')') brackets--;
            if (brackets < 0) { hasError = true; break; }
        }

        if (hasError || brackets != 0) {
            field.setStyle("-fx-border-color: #EF4444; -fx-border-width: 1.5; "
                    + "-fx-background-color: #1A1A1A; -fx-text-fill: #FFF; "
                    + "-fx-font-family: 'JetBrains Mono', 'Consolas', monospace; "
                    + "-fx-font-size: 13; -fx-padding: 6 8;");
        } else {
            field.setStyle("-fx-border-color: #262626; -fx-border-width: 1; "
                    + "-fx-background-color: #1A1A1A; -fx-text-fill: #FFF; "
                    + "-fx-font-family: 'JetBrains Mono', 'Consolas', monospace; "
                    + "-fx-font-size: 13; -fx-padding: 6 8;");
        }
    }

    // ---------------------------------------------------------------
    // 验证与即时反馈
    // ---------------------------------------------------------------

    private void validateAndFire() {
        try {
            new CompiledFunction(xField.getText());
            new CompiledFunction(yField.getText());
            new CompiledFunction(zField.getText());
            errorText.setVisible(false);
            if (onError != null) onError.accept(null);
            if (onFunctionChanged != null) {
                onFunctionChanged.accept(getDefinition());
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            errorText.setText("⚠ " + (msg != null ? msg : "表达式语法错误"));
            errorText.setVisible(true);
            if (onError != null) onError.accept(msg);
        }
    }

    // ---------------------------------------------------------------
    // 自动补全
    // ---------------------------------------------------------------

    private void setupCompletion(TextField field) {
        completionPopup.setOnInsert(insertText -> {
            replaceCurrentWord(field, insertText);
            validateAndFire();
        });

        field.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) completionPopup.hide();
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
                try {
                    double x = field.localToScreen(field.getBoundsInLocal()).getMinX();
                    double y = field.localToScreen(field.getBoundsInLocal()).getMaxY();
                    completionPopup.show(window, x, y, prefix);
                } catch (Exception e) {
                    // 窗口定位失败时忽略
                }
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
    // 工具方法
    // ---------------------------------------------------------------

    private static TextField createCodeField(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.setStyle("-fx-background-color: #1A1A1A; -fx-border-color: #262626; "
                + "-fx-border-radius: 4; -fx-background-radius: 4; "
                + "-fx-text-fill: #FFF; -fx-font-family: 'JetBrains Mono', 'Consolas', monospace; "
                + "-fx-font-size: 13; -fx-padding: 6 8;");
        return tf;
    }

    private static TextField createRangeField(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.setStyle("-fx-background-color: #151515; -fx-border-color: #262626; "
                + "-fx-border-radius: 4; -fx-background-radius: 4; "
                + "-fx-text-fill: #AAA; -fx-font-size: 12; -fx-padding: 6 6;");
        return tf;
    }

    private static double parseDouble(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
