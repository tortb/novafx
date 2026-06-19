package com.novafx.ui.view;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

/**
 * Panel for configuring rendering properties such as point size,
 * point color, background color, and grid visibility.
 */
public final class PropertyPanel extends HBox {

    private final Slider pointSizeSlider = new Slider(0.02, 0.5, 0.08);
    private final ColorPicker pointColorPicker = new ColorPicker(Color.CORNFLOWERBLUE);
    private final CheckBox showGridCheck = new CheckBox("Grid");

    private Consumer<Double> onPointSizeChanged;
    private Consumer<Color> onPointColorChanged;
    private Consumer<Boolean> onShowGridChanged;

    /** Creates the property panel. */
    public PropertyPanel() {
        setSpacing(12);
        setPadding(new Insets(8, 10, 8, 10));
        setStyle("-fx-border-color: #333; -fx-border-width: 1 0 0 0;");

        // Point size
        Label sizeLabel = new Label("Size");
        pointSizeSlider.setShowTickLabels(false);
        pointSizeSlider.setShowTickMarks(false);
        pointSizeSlider.setPrefWidth(120);
        pointSizeSlider.valueProperty().addListener((obs, old, val) -> {
            if (onPointSizeChanged != null) onPointSizeChanged.accept(val.doubleValue());
        });

        // Point color
        Label colorLabel = new Label("Color");
        pointColorPicker.setOnAction(e -> {
            if (onPointColorChanged != null) onPointColorChanged.accept(pointColorPicker.getValue());
        });

        // Show grid
        showGridCheck.setSelected(true);
        showGridCheck.setOnAction(e -> {
            if (onShowGridChanged != null) onShowGridChanged.accept(showGridCheck.isSelected());
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(sizeLabel, pointSizeSlider, colorLabel, pointColorPicker, spacer, showGridCheck);
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
