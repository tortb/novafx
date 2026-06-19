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
 * 右侧参数面板 — 显示可调参数滑块。
 * <p>
 * 当表达式包含自定义参数（如 {@code a*sin(t)} 中的 {@code a}）时，
 * 自动生成滑块。拖动滑块实时重新采样。
 */
public final class ParameterPanel extends VBox {

    private final List<ParameterSlider> sliders = new ArrayList<>();
    private BiConsumer<String, Double> onParameterChanged;

    /** 创建空的参数面板。 */
    public ParameterPanel() {
        setPadding(new Insets(10, 8, 8, 8));
        setSpacing(6);
        setPrefWidth(220);
        setMinWidth(160);
        setStyle("-fx-background-color: #0D0D0D; -fx-border-color: #1A1A1A; -fx-border-width: 0 0 0 1;");

        Label header = new Label(I18n.get("panel.parameters"));
        header.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #CCC; -fx-padding: 0 0 4 0;");
        getChildren().add(header);
    }

    public void setOnParameterChanged(BiConsumer<String, Double> callback) {
        this.onParameterChanged = callback;
    }

    /**
     * 替换当前参数集。
     *
     * @param parameters 新的参数列表；为空时显示"无"
     */
    public void setParameters(List<Parameter> parameters) {
        sliders.clear();
        getChildren().removeIf(node -> node instanceof ParameterSlider);

        if (parameters == null || parameters.isEmpty()) {
            Label empty = new Label(I18n.get("panel.parameters.none"));
            empty.setStyle("-fx-text-fill: #555; -fx-font-size: 11; -fx-padding: 4 0;");
            getChildren().add(empty);
            return;
        }

        for (Parameter p : parameters) {
            ParameterSlider ps = new ParameterSlider(p);
            sliders.add(ps);
            getChildren().add(ps);
        }
    }

    /** 参数滑块控件。 */
    private final class ParameterSlider extends VBox {

        private final String name;
        private final Slider slider;
        private final Label valueLabel;
        private final Label nameLabel;

        ParameterSlider(Parameter param) {
            this.name = param.name();
            setSpacing(2);
            setPadding(new Insets(2, 0, 2, 0));

            nameLabel = new Label(param.name());
            nameLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #F97316;");

            this.slider = new Slider(param.min(), param.max(), param.value());
            slider.setShowTickLabels(false);
            slider.setShowTickMarks(false);
            slider.setBlockIncrement(param.step());
            slider.setStyle("-fx-pref-height: 4;");

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
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.3f", value);
    }
}
