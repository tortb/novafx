package com.novafx.ui.view;

import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

/**
 * Tabbed property panel for configuring particle, render, and function settings.
 * <p>
 * Layout:
 * <ul>
 *   <li>粒子 — point size, color</li>
 *   <li>渲染 — background, grid, axis</li>
 *   <li>函数 — range, step, precision</li>
 * </ul>
 */
public final class PropertyPanel extends TabPane {

    private final Slider pointSizeSlider = new Slider(0.02, 0.5, 0.08);
    private final ColorPicker pointColorPicker = new ColorPicker(Color.CORNFLOWERBLUE);
    private final CheckBox showGridCheck = new CheckBox(I18n.get("panel.properties.grid"));

    private Consumer<Double> onPointSizeChanged;
    private Consumer<Color> onPointColorChanged;
    private Consumer<Boolean> onShowGridChanged;

    /** Creates the property panel. */
    public PropertyPanel() {
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        setStyle("-fx-background-color: #0D0D0D;");

        getTabs().addAll(
                createParticleTab(),
                createRenderTab(),
                createFunctionTab()
        );
    }

    private Tab createParticleTab() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #0D0D0D;");

        // Point size
        Label sizeLabel = new Label(I18n.get("panel.properties.size"));
        sizeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");
        pointSizeSlider.setPrefWidth(200);
        pointSizeSlider.valueProperty().addListener((obs, old, val) -> {
            if (onPointSizeChanged != null) onPointSizeChanged.accept(val.doubleValue());
        });

        // Point color
        Label colorLabel = new Label(I18n.get("panel.properties.color"));
        colorLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");
        pointColorPicker.setOnAction(e -> {
            if (onPointColorChanged != null) onPointColorChanged.accept(pointColorPicker.getValue());
        });

        content.getChildren().addAll(sizeLabel, pointSizeSlider, colorLabel, pointColorPicker);

        Tab tab = new Tab(I18n.get("panel.properties.size"), content);
        tab.setStyle("-fx-background-color: #0D0D0D;");
        return tab;
    }

    private Tab createRenderTab() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #0D0D0D;");

        showGridCheck.setStyle("-fx-font-size: 12;");
        showGridCheck.setSelected(true);
        showGridCheck.setOnAction(e -> {
            if (onShowGridChanged != null) onShowGridChanged.accept(showGridCheck.isSelected());
        });

        content.getChildren().add(showGridCheck);

        Tab tab = new Tab("渲染", content);
        return tab;
    }

    private Tab createFunctionTab() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(12));
        content.setStyle("-fx-background-color: #0D0D0D;");

        Label rangeLabel = new Label("采样范围");
        rangeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");

        HBox rangeBox = new HBox(6);
        TextField startField = new TextField("0");
        TextField endField = new TextField("6.283");
        startField.setPrefWidth(80);
        endField.setPrefWidth(80);
        rangeBox.getChildren().addAll(new Label("起"), startField, new Label("止"), endField);

        Label stepLabel = new Label("步长");
        stepLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");
        TextField stepField = new TextField("0.05");
        stepField.setPrefWidth(80);

        content.getChildren().addAll(rangeLabel, rangeBox, stepLabel, stepField);

        Tab tab = new Tab("函数", content);
        return tab;
    }

    /** Callback when the point size slider changes. */
    public void setOnPointSizeChanged(Consumer<Double> callback) {
        this.onPointSizeChanged = callback;
    }

    /** Callback when the point color changes. */
    public void setOnPointColorChanged(Consumer<Color> callback) {
        this.onPointColorChanged = callback;
    }

    /** Callback when the grid toggle changes. */
    public void setOnShowGridChanged(Consumer<Boolean> callback) {
        this.onShowGridChanged = callback;
    }
}
