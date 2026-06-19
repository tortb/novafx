package com.novafx.ui.view.dialog;

import com.novafx.ui.i18n.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.Map;

/**
 * Modal "New Project" dialog with a project name field and a grid
 * of preset template cards.
 * <p>
 * Returns a {@link Result} when confirmed, or {@code null} when cancelled.
 * <p>
 * Layout:
 * <pre>
 * ┌──────────────────────────────────────┐
 * │  NovaFX — 新建工程                     │
 * ├──────────────────────────────────────┤
 * │  工程名称                             │
 * │  [═══════════════════════════════]   │
 * │                                      │
 * │  模板选择                              │
 * │  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐   │
 * │  │ ♥   │ │ 🌀  │ │ ○   │ │ ★   │   │
 * │  │Heart│ │Spiral│ │Circle│ │Star │   │
 * │  └─────┘ └─────┘ └─────┘ └─────┘   │
 * │                                      │
 * │           [取消]  [创建]              │
 * └──────────────────────────────────────┘
 * </pre>
 */
public final class NewProjectDialog {

    private static final Map<String, String> TEMPLATE_ICONS = Map.ofEntries(
            Map.entry("Circle", "○"),
            Map.entry("Heart", "♥"),
            Map.entry("Star", "★"),
            Map.entry("Spiral", "🌀"),
            Map.entry("DoubleSpiral", "♾"),
            Map.entry("Infinity", "∞"),
            Map.entry("Flower", "✿"),
            Map.entry("Wave", "〰"),
            Map.entry("Helix", "🧬"),
            Map.entry("DNA", "🧬"),
            Map.entry("Sphere", "🌐"),
            Map.entry("Torus", "⭕"),
            Map.entry("Empty", "□")
    );

    private static final Map<String, String> TEMPLATE_LABELS = Map.ofEntries(
            Map.entry("Circle", "圆形"),
            Map.entry("Heart", "爱心"),
            Map.entry("Star", "星形"),
            Map.entry("Spiral", "螺旋"),
            Map.entry("DoubleSpiral", "双螺旋"),
            Map.entry("Infinity", "无穷"),
            Map.entry("Flower", "花朵"),
            Map.entry("Wave", "波浪"),
            Map.entry("Helix", "螺旋线"),
            Map.entry("DNA", "DNA"),
            Map.entry("Sphere", "球体"),
            Map.entry("Torus", "环面"),
            Map.entry("Empty", "空白")
    );

    /**
     * Outcome of a completed dialog.
     *
     * @param projectName the user-entered project name
     * @param template    the selected template identifier
     */
    public record Result(String projectName, String template) {}

    // ---------------------------------------------------------------
    //  Show
    // ---------------------------------------------------------------

    /**
     * Shows the modal dialog and blocks until the user confirms or cancels.
     *
     * @param presetNames the available template names
     * @return the result, or {@code null} if cancelled
     */
    public Result showAndWait(List<String> presetNames) {
        var stage = new Stage(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);

        var result = new Result[1]; // mutable capture

        // ── Root ───────────────────────────────────────────
        var root = new VBox(0);
        root.setStyle("-fx-background-color: #151515; -fx-border-color: #262626; -fx-border-width: 1;");

        // ── Title bar (draggable) ──────────────────────────
        var titleBar = new HBox();
        titleBar.setStyle("-fx-background-color: #0D0D0D; -fx-padding: 8 14;");
        titleBar.setAlignment(Pos.CENTER_LEFT);

        var titleLabel = new Label(I18n.get("dialog.newProject.title"));
        titleLabel.setStyle("-fx-text-fill: #CCC; -fx-font-size: 13; -fx-font-weight: bold;");

        var closeBtn = new Label("✕");
        closeBtn.setStyle(
                "-fx-text-fill: #666; -fx-font-size: 14; -fx-padding: 0 4; -fx-cursor: hand;");
        closeBtn.setOnMouseClicked(e -> stage.close());

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBar.getChildren().addAll(titleLabel, spacer, closeBtn);

        // ── Body ───────────────────────────────────────────
        var body = new VBox(12);
        body.setPadding(new Insets(16, 20, 20, 20));

        // Project name
        var nameLabel = new Label(I18n.get("panel.explorer.newProject"));
        nameLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");

        var nameField = new TextField();
        nameField.setPromptText(I18n.get("dialog.newProject.name"));
        nameField.setStyle(
                "-fx-background-color: #1A1A1A; -fx-border-color: #262626;"
                        + "-fx-border-radius: 4; -fx-background-radius: 4;"
                        + "-fx-text-fill: #FFF; -fx-font-size: 14; -fx-padding: 8 10;");

        // Template selector
        var templateLabel = new Label(I18n.get("dialog.newProject.template"));
        templateLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");

        var cardGrid = new FlowPane();
        cardGrid.setHgap(8);
        cardGrid.setVgap(8);

        var selectedTemplate = new String[]{"Heart"}; // default

        for (String name : presetNames) {
            if ("Empty".equals(name)) continue; // handle separately
            var card = createTemplateCard(name, selectedTemplate);
            cardGrid.getChildren().add(card);
        }
        // Add Empty template
        cardGrid.getChildren().add(createTemplateCard("Empty", selectedTemplate));

        // Button bar
        var buttonBar = new HBox(8);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        var cancelBtn = new Button(I18n.get("menu.file.exit").replace("退出", "取消"));
        cancelBtn.setText("取消");
        cancelBtn.setStyle(
                "-fx-background-color: #1A1A1A; -fx-border-color: #262626;"
                        + "-fx-border-radius: 4; -fx-background-radius: 4;"
                        + "-fx-text-fill: #CCC; -fx-font-size: 13; -fx-padding: 6 20;"
                        + "-fx-cursor: hand;");
        cancelBtn.setOnAction(e -> stage.close());

        var createBtn = new Button("创建");
        createBtn.setStyle(
                "-fx-background-color: #A855F7; -fx-border-color: #A855F7;"
                        + "-fx-border-radius: 4; -fx-background-radius: 4;"
                        + "-fx-text-fill: #FFF; -fx-font-size: 13; -fx-font-weight: bold;"
                        + "-fx-padding: 6 20; -fx-cursor: hand;");
        createBtn.setDefaultButton(true);
        createBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isBlank()) {
                name = selectedTemplate[0];
            }
            result[0] = new Result(name, selectedTemplate[0]);
            stage.close();
        });

        buttonBar.getChildren().addAll(cancelBtn, createBtn);

        body.getChildren().addAll(
                nameLabel, nameField,
                templateLabel, cardGrid,
                buttonBar);

        // ── Build scene ───────────────────────────────────
        root.getChildren().addAll(titleBar, body);
        var scene = new Scene(root, 420, 380);

        // Keyboard: Enter to confirm, Escape to cancel
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) createBtn.fire();
            if (e.getCode() == KeyCode.ESCAPE) stage.close();
        });

        // Drag support for title bar
        var dragDelta = new double[2];
        titleBar.setOnMousePressed(e -> {
            dragDelta[0] = e.getSceneX();
            dragDelta[1] = e.getSceneY();
        });
        titleBar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragDelta[0]);
            stage.setY(e.getScreenY() - dragDelta[1]);
        });

        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.showAndWait();

        return result[0];
    }

    // ---------------------------------------------------------------
    //  Template card
    // ---------------------------------------------------------------

    private VBox createTemplateCard(String name, String[] selectedHolder) {
        String icon = TEMPLATE_ICONS.getOrDefault(name, "□");
        String label = TEMPLATE_LABELS.getOrDefault(name, name);
        boolean isSelected = name.equals(selectedHolder[0]);

        var card = new VBox(4);
        card.setPadding(new Insets(12, 16, 10, 16));
        card.setPrefWidth(82);
        card.setAlignment(Pos.CENTER);
        card.setStyle(cardStyle(isSelected));

        var iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 22; -fx-text-fill: " + (isSelected ? "#FFF" : "#A855F7") + ";");

        var nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 11; -fx-text-fill: " + (isSelected ? "#FFF" : "#AAA") + ";");

        card.getChildren().addAll(iconLabel, nameLabel);

        card.setOnMouseEntered(e -> {
            if (!isSelected) card.setStyle(
                    "-fx-background-color: #1A1A1A; -fx-background-radius: 6;"
                            + "-fx-border-color: #333; -fx-border-radius: 6;"
                            + "-fx-cursor: hand;");
        });
        card.setOnMouseExited(e -> {
            if (!isSelected) card.setStyle(cardStyle(false));
        });

        card.setOnMouseClicked(e -> {
            selectedHolder[0] = name;
            // Update all cards (simple approach: rebuild parent)
            FlowPane parent = (FlowPane) card.getParent();
            if (parent != null) {
                parent.getChildren().replaceAll(c ->
                        c instanceof VBox v ? rebuildCard(
                                v, name.equals(getCardName(v)), selectedHolder)
                                : c);
            }
        });

        return card;
    }

    private static String getCardName(VBox card) {
        var labels = card.getChildren().filtered(c -> c instanceof Label);
        if (labels.size() >= 2) {
            return ((Label) labels.get(1)).getText();
        }
        return "";
    }

    private static VBox rebuildCard(VBox old, boolean selected, String[] holder) {
        var labels = old.getChildren().filtered(c -> c instanceof Label);
        if (labels.size() < 2) return old;

        var iconLabel = (Label) labels.get(0);
        var nameLabel = (Label) labels.get(1);

        old.setStyle(cardStyle(selected));
        iconLabel.setStyle("-fx-font-size: 22; -fx-text-fill: "
                + (selected ? "#FFF" : "#A855F7") + ";");
        nameLabel.setStyle("-fx-font-size: 11; -fx-text-fill: "
                + (selected ? "#FFF" : "#AAA") + ";");

        return old;
    }

    private static String cardStyle(boolean selected) {
        if (selected) {
            return "-fx-background-color: #A855F7; -fx-background-radius: 6;"
                    + "-fx-border-color: #A855F7; -fx-border-radius: 6;"
                    + "-fx-cursor: hand;";
        }
        return "-fx-background-color: #111111; -fx-background-radius: 6;"
                + "-fx-border-color: #1A1A1A; -fx-border-radius: 6;"
                + "-fx-cursor: hand;";
    }
}
