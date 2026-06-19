package com.novafx.ui.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Left-side panel listing available presets.
 * <p>
 * Clicking a preset notifies the registered callback with the preset name.
 */
public final class PresetPanel extends VBox {

    private final ListView<String> listView = new ListView<>();

    private Consumer<String> onPresetSelected;

    /** Creates the preset panel with the given preset names. */
    public PresetPanel(List<String> presetNames) {
        setPadding(new Insets(8));
        setSpacing(6);
        setPrefWidth(160);
        setMinWidth(120);

        Label header = new Label("Presets");
        header.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        listView.setItems(FXCollections.observableArrayList(presetNames));
        listView.setPrefHeight(300);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null && onPresetSelected != null) {
                onPresetSelected.accept(selected);
            }
        });

        getChildren().addAll(header, listView);
        VBox.setVgrow(listView, javafx.scene.layout.Priority.ALWAYS);
    }

    /** Sets the callback invoked when a preset is selected. */
    public void setOnPresetSelected(Consumer<String> callback) {
        this.onPresetSelected = callback;
    }
}
