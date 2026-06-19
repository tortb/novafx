package com.novafx.ui.components;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * 停靠区域指示器 — UI Kit v2 辅助组件。
 * <p>
 * 当拖拽面板时，显示可停靠的区域和预览效果。
 * 提供视觉反馈，帮助用户了解可以停靠的位置。
 */
public class DockDropZone extends StackPane {

    /** 停靠区域位置 */
    private final DraggableDockPane.DockPosition position;

    /** 区域指示器 */
    private Rectangle indicator;

    /** 区域标签 */
    private Text label;

    /** 是否激活 */
    private boolean active = false;

    /**
     * 创建停靠区域指示器实例。
     *
     * @param position 停靠位置
     * @param width    区域宽度
     * @param height   区域高度
     */
    public DockDropZone(DraggableDockPane.DockPosition position, double width, double height) {
        this.position = position;
        getStyleClass().add("dock-drop-zone");
        initIndicator(width, height);
    }

    /**
     * 初始化区域指示器。
     *
     * @param width  区域宽度
     * @param height 区域高度
     */
    private void initIndicator(double width, double height) {
        // 创建半透明背景
        Rectangle background = new Rectangle(width, height);
        background.setFill(Color.web("#A855F7", 0.1));
        background.setStroke(Color.web("#A855F7", 0.3));
        background.setStrokeWidth(2);
        background.setArcWidth(8);
        background.setArcHeight(8);

        // 创建指示器
        indicator = new Rectangle(width - 4, height - 4);
        indicator.setFill(Color.web("#A855F7", 0.2));
        indicator.setStroke(Color.web("#A855F7", 0.5));
        indicator.setStrokeWidth(1);
        indicator.setArcWidth(6);
        indicator.setArcHeight(6);
        indicator.setVisible(false);

        // 创建标签
        label = new Text(getPositionLabel());
        label.setFill(Color.web("#A855F7", 0.8));
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        label.setVisible(false);

        // 布局
        getChildren().addAll(background, indicator, label);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(4));
    }

    /**
     * 获取位置标签文本。
     *
     * @return 位置标签
     */
    private String getPositionLabel() {
        return switch (position) {
            case LEFT -> "◄ Left";
            case RIGHT -> "Right ►";
            case TOP -> "▲ Top";
            case BOTTOM -> "Bottom ▼";
            case CENTER -> "Center";
        };
    }

    /**
     * 激活停靠区域（显示高亮）。
     */
    public void activate() {
        if (!active) {
            active = true;
            indicator.setVisible(true);
            label.setVisible(true);
            setEffect(new DropShadow(10, Color.web("#A855F7", 0.5)));
        }
    }

    /**
     * 停用停靠区域（隐藏高亮）。
     */
    public void deactivate() {
        if (active) {
            active = false;
            indicator.setVisible(false);
            label.setVisible(false);
            setEffect(null);
        }
    }

    /**
     * 检查是否激活。
     *
     * @return true 表示激活
     */
    public boolean isActive() {
        return active;
    }

    /**
     * 获取停靠位置。
     *
     * @return 停靠位置
     */
    public DraggableDockPane.DockPosition getPosition() {
        return position;
    }

    /**
     * 检查指定点是否在停靠区域内。
     *
     * @param x X 坐标
     * @param y Y 坐标
     * @return true 表示在区域内
     */
    public boolean containsPoint(double x, double y) {
        Bounds bounds = localToScene(getBoundsInLocal());
        return bounds.contains(x, y);
    }
}
