package com.novafx.ui.view;

import com.novafx.function.Parameter;
import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Right-side panel that displays adjustable parameters as sliders.
 * <p>
 * Automatically updates when the function expression changes.
 * Dragging a slider triggers real-time re-sampling via the registered callback.
 */
public final class ParameterPanel extends VBox {

    private final List<ParameterSlider> sliders = new ArrayList<>();
    private BiConsumer<String, Double> onParameterChanged;

    /** Creates an empty parameter panel. */
    public ParameterPanel() {
        setPadding(new Insets(10));
        setSpacing(8);
        setPrefWidth(220);
        setMinWidth(160);

        Label header = new Label(I18n.get("panel.parameters"));
        header.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        getChildren().add(header);
    }

    /**
     * Sets the callback invoked when a parameter slider is dragged.
     *
     * @param callback receives (parameterName, newValue)
     */
    public void setOnParameterChanged(BiConsumer<String, Double> callback) {
        this.onParameterChanged = callback;
    }

    /**
     * Replaces the current parameter set with new values.
     *
     * @param parameters the new parameter list
     */
    public void setParameters(List<Parameter> parameters) {
        sliders.clear();
        getChildren().removeIf(node -> node instanceof ParameterSlider);

        if (parameters == null || parameters.isEmpty()) {
            Label empty = new Label(I18n.get("panel.parameters.none"));
            empty.setStyle("-fx-text-fill: #666;");
            getChildren().add(empty);
            return;
        }

        for (Parameter p : parameters) {
            ParameterSlider ps = new ParameterSlider(p);
            sliders.add(ps);
            getChildren().add(ps);
        }
    }

    // ---------------------------------------------------------------
    // Parameter slider control
    // ---------------------------------------------------------------

    private final class ParameterSlider extends VBox {

        private final String name;
        private final Slider slider;
        private final Label valueLabel;

        ParameterSlider(Parameter param) {
            this.name = param.name();
            setSpacing(2);

            Label nameLabel = new Label(param.name());
            nameLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #ccc;");

            this.slider = new Slider(param.min(), param.max(), param.value());
            slider.setShowTickLabels(false);
            slider.setShowTickMarks(false);
            slider.setBlockIncrement(param.step());

            this.valueLabel = new Label(formatValue(param.value()));
            valueLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #888;");

            slider.valueProperty().addListener((obs, old, val) -> {
                valueLabel.setText(formatValue(val.doubleValue()));
                if (onParameterChanged != null) {
                    onParameterChanged.accept(name, val.doubleValue());
                }
            });

            getChildren().addAll(nameLabel, slider, valueLabel);
        }
    }

    private static String formatValue(double value) {
        return String.format("%.3f", value);
    }
}
