package com.novafx.ui.view;

import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

/**
 * Application toolbar replacing the old {@link javafx.scene.control.MenuBar}.
 * <p>
 * Layout:
 * <pre>
 * ┌──────────────────────────────────────────────────────────┐
 * │ [NovaFX]  Project Name     │  [New] [Open] [Save] [Undo/Redo] [2D|3D] [≡] │
 * └──────────────────────────────────────────────────────────┘
 * </pre>
 */
public final class TopBar extends HBox {

    private final Label projectNameLabel = new Label(I18n.get("topbar.projectName"));
    private final ToggleGroup dimensionGroup = new ToggleGroup();
    private final ToggleButton dim2Btn = new ToggleButton(I18n.get("topbar.dim2"));
    private final ToggleButton dim3Btn = new ToggleButton(I18n.get("topbar.dim3"));

    private Runnable onNewProject = () -> {};
    private Runnable onOpenProject = () -> {};
    private Runnable onSaveProject = () -> {};
    private Runnable onUndo = () -> {};
    private Runnable onRedo = () -> {};
    private Runnable on2DMode = () -> {};
    private Runnable on3DMode = () -> {};
    private Consumer<CameraPreset> onCameraPreset = p -> {};
    private Runnable onToggleLeftPanel = () -> {};
    private Runnable onToggleRightPanel = () -> {};

    public enum CameraPreset { TOP, FRONT, SIDE, PERSPECTIVE, ISOMETRIC }

    public TopBar() {
        getStyleClass().add("top-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(0, 8, 0, 8));
        setMinHeight(40);
        setPrefHeight(40);

        // ── Left: brand + project name ──
        Label brand = new Label("NovaFX");
        brand.setStyle("-fx-text-fill: #A855F7; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 0 12 0 4;");

        projectNameLabel.setStyle("-fx-text-fill: #CCC; -fx-font-size: 13px;");

        HBox leftGroup = new HBox(4, brand, projectNameLabel);
        leftGroup.setAlignment(Pos.CENTER_LEFT);

        // ── Center spacer ──
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ── Right: actions + dimension toggle ──
        Button newBtn = createTopButton(I18n.get("topbar.newProject"), "📄");
        newBtn.setOnAction(e -> onNewProject.run());

        Button openBtn = createTopButton(I18n.get("topbar.openProject"), "📂");
        openBtn.setOnAction(e -> onOpenProject.run());

        Button saveBtn = createTopButton(I18n.get("topbar.saveProject"), "💾");
        saveBtn.setOnAction(e -> onSaveProject.run());

        // Separator
        Region sep1 = createSeparator();

        Button undoBtn = createTopButton(I18n.get("topbar.undo"), "↩");
        undoBtn.setOnAction(e -> onUndo.run());

        Button redoBtn = createTopButton(I18n.get("topbar.redo"), "↪");
        redoBtn.setOnAction(e -> onRedo.run());

        // Separator
        Region sep2 = createSeparator();

        dim2Btn.setToggleGroup(dimensionGroup);
        dim2Btn.setSelected(true);
        dim2Btn.setStyle(topToggleStyle());
        dim2Btn.setOnAction(e -> on2DMode.run());

        dim3Btn.setToggleGroup(dimensionGroup);
        dim3Btn.setStyle(topToggleStyle());
        dim3Btn.setOnAction(e -> on3DMode.run());

        // Separator
        Region sep3 = createSeparator();

        Button toggleLeftBtn = createTopButton("", "◀");
        toggleLeftBtn.setOnAction(e -> onToggleLeftPanel.run());

        Button toggleRightBtn = createTopButton("", "▶");
        toggleRightBtn.setOnAction(e -> onToggleRightPanel.run());

        HBox rightGroup = new HBox(2,
                newBtn, openBtn, saveBtn, sep1,
                undoBtn, redoBtn, sep2,
                dim2Btn, dim3Btn, sep3,
                toggleLeftBtn, toggleRightBtn
        );
        rightGroup.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(leftGroup, spacer, rightGroup);
    }

    // ── Setters for callbacks ──

    public void setOnNewProject(Runnable cb) { this.onNewProject = cb; }
    public void setOnOpenProject(Runnable cb) { this.onOpenProject = cb; }
    public void setOnSaveProject(Runnable cb) { this.onSaveProject = cb; }
    public void setOnUndo(Runnable cb) { this.onUndo = cb; }
    public void setOnRedo(Runnable cb) { this.onRedo = cb; }
    public void setOn2DMode(Runnable cb) { this.on2DMode = cb; }
    public void setOn3DMode(Runnable cb) { this.on3DMode = cb; }
    public void setOnCameraPreset(Consumer<CameraPreset> cb) { this.onCameraPreset = cb; }
    public void setOnToggleLeftPanel(Runnable cb) { this.onToggleLeftPanel = cb; }
    public void setOnToggleRightPanel(Runnable cb) { this.onToggleRightPanel = cb; }

    public void setProjectName(String name) {
        projectNameLabel.setText(name != null && !name.isBlank() ? name : I18n.get("topbar.projectName"));
    }

    public void setDimensionMode(boolean is3D) {
        if (is3D) {
            dim2Btn.setSelected(false);
            dim3Btn.setSelected(true);
        } else {
            dim2Btn.setSelected(true);
            dim3Btn.setSelected(false);
        }
    }

    // ── Style helpers ──

    private static Button createTopButton(String tooltip, String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("top-bar-button");
        btn.setStyle(topButtonStyle());
        return btn;
    }

    private static Region createSeparator() {
        Region sep = new Region();
        sep.setStyle("-fx-background-color: #262626; -fx-min-width: 1; -fx-min-height: 20; -fx-padding: 0 4;");
        return sep;
    }

    private static String topButtonStyle() {
        return "-fx-background-color: transparent; -fx-border-color: transparent;"
                + "-fx-text-fill: #888; -fx-font-size: 12px; -fx-padding: 4 6;"
                + "-fx-min-height: 26px; -fx-cursor: hand;";
    }

    private static String topToggleStyle() {
        return "-fx-background-color: transparent; -fx-border-color: #262626;"
                + "-fx-text-fill: #888; -fx-font-size: 11px; -fx-padding: 2 8;"
                + "-fx-min-height: 24px; -fx-cursor: hand;"
                + "-fx-border-radius: 4; -fx-background-radius: 4;";
    }
}
