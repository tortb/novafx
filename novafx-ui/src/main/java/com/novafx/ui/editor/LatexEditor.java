package com.novafx.ui.editor;

import com.novafx.math.FunctionDefinition;
import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.function.Consumer;

/**
 * LaTeX 编辑器 — SplitPane 50/50 布局。
 * <p>
 * 左侧：LaTeX 源码编辑
 * 右侧：公式实时预览
 * 支持窗口缩放，禁止内容溢出。
 */
public final class LatexEditor extends SplitPane {

    private final TextField xField = createLatexField("\\cos(t)");
    private final TextField yField = createLatexField("\\sin(t)");
    private final TextField zField = createLatexField("0");
    private final TextField startField = createRangeField("0");
    private final TextField endField = createRangeField("6.283");
    private final TextField stepField = createRangeField("0.05");

    private final TextFlow xPreview = new TextFlow();
    private final TextFlow yPreview = new TextFlow();
    private final TextFlow zPreview = new TextFlow();
    private final Text errorText = new Text();

    private Consumer<FunctionDefinition> onFunctionChanged;
    private Consumer<String> onError;

    /** 创建 LaTeX 编辑器。 */
    public LatexEditor() {
        setOrientation(Orientation.HORIZONTAL);
        setDividerPositions(0.5);
        setStyle("-fx-background-color: #0D0D0D;");

        // ── 左侧：源码编辑 ──
        VBox sourcePane = new VBox(4);
        sourcePane.setPadding(new Insets(6, 8, 6, 8));
        sourcePane.setStyle("-fx-background-color: #0D0D0D;");

        Label srcTitle = new Label("LaTeX 源码");
        srcTitle.setStyle("-fx-font-size: 11; -fx-text-fill: #666; -fx-font-weight: bold;");

        sourcePane.getChildren().add(srcTitle);

        sourcePane.getChildren().add(createLatexRow("X(t)", xField, I18n.get("editor.start"), startField));
        sourcePane.getChildren().add(createLatexRow("Y(t)", yField, I18n.get("editor.end"), endField));
        sourcePane.getChildren().add(createLatexRow("Z(t)", zField, I18n.get("editor.step"), stepField));

        // 错误提示
        errorText.setStyle("-fx-fill: #EF4444; -fx-font-size: 11; -fx-padding: 2 0 0 4;");
        errorText.setVisible(false);
        sourcePane.getChildren().add(errorText);

        // ── 右侧：公式预览 ──
        VBox previewPane = new VBox(8);
        previewPane.setPadding(new Insets(6, 8, 6, 8));
        previewPane.setStyle("-fx-background-color: #0D0D0D; -fx-border-color: #1A1A1A; -fx-border-width: 0 0 0 1;");

        Label previewTitle = new Label("公式预览");
        previewTitle.setStyle("-fx-font-size: 11; -fx-text-fill: #666; -fx-font-weight: bold;");

        previewPane.getChildren().add(previewTitle);

        // 预览卡片
        for (var entry : new String[][]{
                {"X(t)", ""}, {"Y(t)", ""}, {"Z(t)", ""}
        }) {
            VBox card = new VBox(2);
            card.setPadding(new Insets(8, 10, 8, 10));
            card.setStyle("-fx-background-color: #111; -fx-border-color: #1A1A1A; "
                    + "-fx-border-radius: 6; -fx-background-radius: 6;");

            Label cardTitle = new Label(entry[0]);
            cardTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #F97316; -fx-font-size: 12;");

            TextFlow preview = switch (entry[0]) {
                case "X(t)" -> xPreview;
                case "Y(t)" -> yPreview;
                default -> zPreview;
            };
            preview.setStyle("-fx-padding: 4 0 0 0;");

            card.getChildren().addAll(cardTitle, preview);
            VBox.setVgrow(card, Priority.NEVER);
            previewPane.getChildren().add(card);
        }

        // 预览区域填满剩余空间
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        previewPane.getChildren().add(spacer);

        getItems().addAll(sourcePane, previewPane);

        // 预览样式
        for (var p : new TextFlow[]{xPreview, yPreview, zPreview}) {
            p.setStyle("-fx-fill: #AAA; -fx-font-size: 13; -fx-font-family: 'Times New Roman', serif;");
        }

        // 字段事件 — 即时更新
        for (var field : new TextField[]{xField, yField, zField}) {
            field.textProperty().addListener((obs, old, nw) -> {
                if (field.isFocused()) {
                    updatePreviews();
                    fireChanged();
                }
            });
        }

        for (var field : new TextField[]{startField, endField, stepField}) {
            field.setOnAction(e -> fireChanged());
            field.focusedProperty().addListener((obs, old, nw) -> {
                if (Boolean.FALSE.equals(nw)) fireChanged();
            });
        }

        updatePreviews();
    }

    public void setOnFunctionChanged(Consumer<FunctionDefinition> callback) {
        this.onFunctionChanged = callback;
    }

    public void setOnError(Consumer<String> callback) {
        this.onError = callback;
    }

    public void loadDefinition(FunctionDefinition def) {
        if (def == null) return;
        xField.setText(toLatex(def.xExpression()));
        yField.setText(toLatex(def.yExpression()));
        zField.setText(toLatex(def.zExpression()));
        startField.setText(String.valueOf(def.start()));
        endField.setText(String.valueOf(def.end()));
        stepField.setText(String.valueOf(def.step()));
        updatePreviews();
        errorText.setVisible(false);
    }

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

    public void clearError() {
        errorText.setVisible(false);
    }

    // ---------------------------------------------------------------
    // 布局
    // ---------------------------------------------------------------

    private HBox createLatexRow(String label, TextField codeField,
                                String rangeLabel, TextField rangeField) {
        HBox row = new HBox(6);
        row.setPadding(new Insets(2, 0, 2, 0));
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #F97316; "
                + "-fx-font-size: 13; -fx-min-width: 32;");

        HBox.setHgrow(codeField, Priority.ALWAYS);
        codeField.setPrefHeight(32);
        codeField.setMinHeight(32);

        Label rangeLbl = new Label(rangeLabel);
        rangeLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #666; -fx-min-width: 24;");

        rangeField.setPrefWidth(72);
        rangeField.setPrefHeight(32);

        row.getChildren().addAll(lbl, codeField, rangeLbl, rangeField);
        row.setPrefHeight(36);
        return row;
    }

    // ---------------------------------------------------------------
    // LaTeX ↔ 表达式转换（委托给 LatexUtils）
    // ---------------------------------------------------------------

    static String fromLatex(String latex) {
        return LatexUtils.fromLatex(latex);
    }

    static String toLatex(String expr) {
        return LatexUtils.toLatex(expr);
    }

    private void updatePreviews() {
        setPreview(xPreview, xField.getText());
        setPreview(yPreview, yField.getText());
        setPreview(zPreview, zField.getText());
    }

    private void setPreview(TextFlow flow, String latex) {
        flow.getChildren().clear();
        String display = latex.isBlank() ? "（空）" : latex;
        Text t = new Text(display);
        t.setStyle("-fx-fill: #AAA; -fx-font-size: 14; -fx-font-family: 'Times New Roman', serif;");
        flow.getChildren().add(t);
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

    private static TextField createLatexField(String defaultValue) {
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
