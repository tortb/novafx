package com.novafx.ui.view;

import com.novafx.ui.components.FXPanel;
import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

/**
 * 属性面板 — 粒子 / 渲染 / 函数 设置。
 * <p>
 * 标签式布局，暗色主题，统一间距 8px。
 */
public final class PropertyPanel extends TabPane {

    private final Slider pointSizeSlider = new Slider(0.02, 0.5, 0.08);
    private final ColorPicker pointColorPicker = new ColorPicker(Color.CORNFLOWERBLUE);
    private final CheckBox showGridCheck = new CheckBox(I18n.get("panel.properties.grid"));

    private Consumer<Double> onPointSizeChanged;
    private Consumer<Color> onPointColorChanged;
    private Consumer<Boolean> onShowGridChanged;

    public PropertyPanel() {
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        setStyle("-fx-background-color: #0D0D0D;");

        getTabs().addAll(
                createTab("粒子", createParticleContent()),
                createTab("渲染", createRenderContent()),
                createTab("函数", createFunctionContent())
        );
    }

    private Tab createTab(String title, VBox content) {
        Tab tab = new Tab(title, content);
        tab.setStyle("-fx-background-color: #0D0D0D;");
        return tab;
    }

    private VBox tabContent() {
        FXPanel panel = new FXPanel() {};
        panel.setPadding(new Insets(12, 10, 12, 10));
        panel.setSpacing(8);
        return panel;
    }

    // ── 粒子 ──

    private VBox createParticleContent() {
        VBox content = tabContent();

        Label sizeLabel = new Label(I18n.get("panel.properties.size"));
        sizeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");

        pointSizeSlider.setPrefWidth(180);
        pointSizeSlider.valueProperty().addListener((obs, old, val) -> {
            if (onPointSizeChanged != null) onPointSizeChanged.accept(val.doubleValue());
        });

        Label colorLabel = new Label(I18n.get("panel.properties.color"));
        colorLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");

        pointColorPicker.setPrefWidth(180);
        pointColorPicker.setStyle("-fx-background-color: #1A1A1A; -fx-border-color: #262626; -fx-border-radius: 4;");
        pointColorPicker.setOnAction(e -> {
            if (onPointColorChanged != null) onPointColorChanged.accept(pointColorPicker.getValue());
        });

        content.getChildren().addAll(sizeLabel, pointSizeSlider, colorLabel, pointColorPicker);
        return content;
    }

    // ── 渲染 ──

    private VBox createRenderContent() {
        VBox content = tabContent();

        showGridCheck.setStyle("-fx-font-size: 12; -fx-text-fill: #CCC;");
        showGridCheck.setSelected(true);
        showGridCheck.setOnAction(e -> {
            if (onShowGridChanged != null) onShowGridChanged.accept(showGridCheck.isSelected());
        });

        content.getChildren().add(showGridCheck);
        return content;
    }

    // ── 函数 ──

    private VBox createFunctionContent() {
        VBox content = tabContent();

        Label rangeLabel = new Label("采样范围");
        rangeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");

        javafx.scene.layout.HBox rangeBox = new javafx.scene.layout.HBox(6);
        TextField startField = new TextField("0");
        TextField endField = new TextField("6.283");
        startField.setPrefWidth(80);
        endField.setPrefWidth(80);
        startField.setStyle("-fx-background-color: #1A1A1A; -fx-border-color: #262626; "
                + "-fx-border-radius: 4; -fx-text-fill: #FFF; -fx-font-size: 12; -fx-padding: 6 6;");
        endField.setStyle("-fx-background-color: #1A1A1A; -fx-border-color: #262626; "
                + "-fx-border-radius: 4; -fx-text-fill: #FFF; -fx-font-size: 12; -fx-padding: 6 6;");

        Label startLbl = new Label("起");
        startLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");
        Label endLbl = new Label("止");
        endLbl.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");

        rangeBox.getChildren().addAll(startLbl, startField, endLbl, endField);

        Label stepLabel = new Label("步长");
        stepLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");
        TextField stepField = new TextField("0.05");
        stepField.setPrefWidth(80);
        stepField.setStyle("-fx-background-color: #1A1A1A; -fx-border-color: #262626; "
                + "-fx-border-radius: 4; -fx-text-fill: #FFF; -fx-font-size: 12; -fx-padding: 6 6;");

        content.getChildren().addAll(rangeLabel, rangeBox, stepLabel, stepField);
        return content;
    }

    // ── 回调 ──

    public void setOnPointSizeChanged(Consumer<Double> callback) {
        this.onPointSizeChanged = callback;
    }

    public void setOnPointColorChanged(Consumer<Color> callback) {
        this.onPointColorChanged = callback;
    }

    public void setOnShowGridChanged(Consumer<Boolean> callback) {
        this.onShowGridChanged = callback;
    }
}
