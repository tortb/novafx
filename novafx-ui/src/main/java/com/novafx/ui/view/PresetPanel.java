package com.novafx.ui.view;

import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Left-side panel listing available presets in a card-based layout.
 * <p>
 * Each preset is displayed as a card with an emoji icon and Chinese name.
 * Inspired by Blender's asset browser.
 */
public final class PresetPanel extends VBox {

    private static final Map<String, String> ICONS = Map.ofEntries(
            Map.entry("Circle", "○"),
            Map.entry("Heart", "♥"),
            Map.entry("Star", "★"),
            Map.entry("Spiral", "🌀"),
            Map.entry("DoubleSpiral", "♾"),
            Map.entry("Infinity", "∞"),
            Map.entry("Flower", "✿"),
            Map.entry("Wave", "〰"),
            Map.entry("Helix", "🧬")
    );

    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("Circle", "圆形"),
            Map.entry("Heart", "爱心"),
            Map.entry("Star", "星形"),
            Map.entry("Spiral", "螺旋"),
            Map.entry("DoubleSpiral", "双螺旋"),
            Map.entry("Infinity", "无穷"),
            Map.entry("Flower", "花朵"),
            Map.entry("Wave", "波浪"),
            Map.entry("Helix", "螺旋线")
    );

    private final FlowPane cardGrid = new FlowPane();
    private Consumer<String> onPresetSelected;

    /** Creates the preset panel with the given preset names. */
    public PresetPanel(List<String> presetNames) {
        setPadding(new Insets(0));
        setSpacing(0);
        setPrefWidth(220);
        setMinWidth(180);
        setStyle("-fx-background-color: #0D0D0D; -fx-border-color: #1A1A1A; -fx-border-width: 0 1 0 0;");

        Label header = new Label(I18n.get("panel.presets"));
        header.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 10 12 6 12; -fx-text-fill: #888;");

        cardGrid.setPadding(new Insets(4, 8, 8, 8));
        cardGrid.setHgap(6);
        cardGrid.setVgap(6);
        cardGrid.setPrefWrapLength(200);

        for (String name : presetNames) {
            cardGrid.getChildren().add(createCard(name));
        }

        ScrollPane scroll = new ScrollPane(cardGrid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        getChildren().addAll(header, scroll);
        VBox.setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);
    }

    /** Sets the callback invoked when a preset is selected. */
    public void setOnPresetSelected(Consumer<String> callback) {
        this.onPresetSelected = callback;
    }

    private VBox createCard(String name) {
        String icon = ICONS.getOrDefault(name, "▷");
        String label = LABELS.getOrDefault(name, name);

        VBox card = new VBox(4);
        card.setPadding(new Insets(10, 8, 8, 8));
        card.setPrefWidth(90);
        card.setStyle(
                "-fx-background-color: #111111;"
                        + "-fx-background-radius: 6;"
                        + "-fx-border-color: #1A1A1A;"
                        + "-fx-border-radius: 6;"
                        + "-fx-cursor: hand;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20; -fx-text-fill: #F97316;");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #AAA;");

        card.getChildren().addAll(iconLabel, nameLabel);

        card.setOnMouseEntered(e ->
                card.setStyle(
                        "-fx-background-color: #1A1A1A;"
                                + "-fx-background-radius: 6;"
                                + "-fx-border-color: #F97316;"
                                + "-fx-border-radius: 6;"
                                + "-fx-cursor: hand;"
                )
        );
        card.setOnMouseExited(e ->
                card.setStyle(
                        "-fx-background-color: #111111;"
                                + "-fx-background-radius: 6;"
                                + "-fx-border-color: #1A1A1A;"
                                + "-fx-border-radius: 6;"
                                + "-fx-cursor: hand;"
                )
        );

        card.setOnMouseClicked(e -> {
            if (onPresetSelected != null) {
                onPresetSelected.accept(name);
            }
        });

        return card;
    }
}
