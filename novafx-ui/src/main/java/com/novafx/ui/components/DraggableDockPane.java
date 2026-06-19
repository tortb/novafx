package com.novafx.ui.components;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * 可拖拽的 DockPane — UI Kit v2 核心组件。
 * <p>
 * 支持面板的拖拽重排功能，实现 VSCode 风格的 Dock 布局。
 * 提供拖拽预览和停靠区域高亮。
 */
public class DraggableDockPane extends DockPane {

    /** 当前正在拖拽的节点 */
    private Node draggingNode;

    /** 拖拽起始偏移量 */
    private double dragOffsetX;
    private double dragOffsetY;

    /** 拖拽预览效果 */
    private DropShadow dragEffect;

    /** 停靠区域高亮颜色 */
    private static final Color DOCK_HIGHLIGHT_COLOR = Color.web("#A855F7", 0.3);

    /**
     * 创建可拖拽的 DockPane 实例。
     */
    public DraggableDockPane() {
        super();
        getStyleClass().add("draggable-dock-pane");
        initDragEffect();
    }

    /**
     * 初始化拖拽效果。
     */
    private void initDragEffect() {
        dragEffect = new DropShadow();
        dragEffect.setColor(Color.web("#A855F7", 0.5));
        dragEffect.setRadius(10);
        dragEffect.setOffsetX(0);
        dragEffect.setOffsetY(3);
    }

    /**
     * 为指定节点启用拖拽功能。
     *
     * @param node 要启用拖拽的节点
     */
    public void enableDrag(Node node) {
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, this::onDragStart);
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onDrag);
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, this::onDragEnd);
    }

    /**
     * 拖拽开始事件处理。
     *
     * @param event 鼠标事件
     */
    private void onDragStart(MouseEvent event) {
        if (event.getSource() instanceof Node) {
            draggingNode = (Node) event.getSource();
            dragOffsetX = event.getX();
            dragOffsetY = event.getY();

            // 添加拖拽效果
            draggingNode.setEffect(dragEffect);
            draggingNode.setOpacity(0.8);

            // 提升层级
            draggingNode.toFront();

            event.consume();
        }
    }

    /**
     * 拖拽中事件处理。
     *
     * @param event 鼠标事件
     */
    private void onDrag(MouseEvent event) {
        if (draggingNode != null) {
            // 计算新位置
            double newX = event.getSceneX() - dragOffsetX;
            double newY = event.getSceneY() - dragOffsetY;

            // 更新位置
            draggingNode.setLayoutX(newX);
            draggingNode.setLayoutY(newY);

            // 检查停靠区域
            checkDockZones(event);

            event.consume();
        }
    }

    /**
     * 拖拽结束事件处理。
     *
     * @param event 鼠标事件
     */
    private void onDragEnd(MouseEvent event) {
        if (draggingNode != null) {
            // 移除拖拽效果
            draggingNode.setEffect(null);
            draggingNode.setOpacity(1.0);

            // 确定最终停靠位置
            DockPosition position = getDockPosition(event);
            if (position != null) {
                dockNode(draggingNode, position);
            }

            draggingNode = null;
            event.consume();
        }
    }

    /**
     * 检查停靠区域并高亮显示。
     *
     * @param event 鼠标事件
     */
    private void checkDockZones(MouseEvent event) {
        // 这里可以添加停靠区域的高亮逻辑
        // 当拖拽节点接近某个停靠区域时，高亮显示该区域
    }

    /**
     * 获取鼠标位置对应的停靠位置。
     *
     * @param event 鼠标事件
     * @return 停靠位置，如果不在任何停靠区域则返回 null
     */
    private DockPosition getDockPosition(MouseEvent event) {
        double x = event.getSceneX();
        double y = event.getSceneY();

        // 获取各个停靠区域的边界
        Bounds leftBounds = getDockLeft() != null ? getDockLeft().localToScene(getDockLeft().getBoundsInLocal()) : null;
        Bounds rightBounds = getDockRight() != null ? getDockRight().localToScene(getDockRight().getBoundsInLocal()) : null;
        Bounds topBounds = getTop() != null ? getTop().localToScene(getTop().getBoundsInLocal()) : null;
        Bounds bottomBounds = getDockBottom() != null ? getDockBottom().localToScene(getDockBottom().getBoundsInLocal()) : null;

        // 检查是否在左侧停靠区域
        if (leftBounds != null && x < leftBounds.getMaxX() + 50) {
            return DockPosition.LEFT;
        }

        // 检查是否在右侧停靠区域
        if (rightBounds != null && x > rightBounds.getMinX() - 50) {
            return DockPosition.RIGHT;
        }

        // 检查是否在顶部停靠区域
        if (topBounds != null && y < topBounds.getMaxY() + 50) {
            return DockPosition.TOP;
        }

        // 检查是否在底部停靠区域
        if (bottomBounds != null && y > bottomBounds.getMinY() - 50) {
            return DockPosition.BOTTOM;
        }

        return null;
    }

    /**
     * 将节点停靠到指定位置。
     *
     * @param node     要停靠的节点
     * @param position 停靠位置
     */
    private void dockNode(Node node, DockPosition position) {
        // 移除当前位置
        if (getDockLeft() == node) {
            setDockLeft(null);
        } else if (getDockRight() == node) {
            setDockRight(null);
        } else if (getDockBottom() == node) {
            setDockBottom(null);
        } else if (getCenter() == node) {
            setCenter(null);
        }

        // 添加到新位置
        switch (position) {
            case LEFT:
                setDockLeft(node);
                break;
            case RIGHT:
                setDockRight(node);
                break;
            case TOP:
                setTop(node);
                break;
            case BOTTOM:
                setDockBottom(node);
                break;
            case CENTER:
                setDockCenter(node);
                break;
        }

        // 重置布局位置
        node.setLayoutX(0);
        node.setLayoutY(0);
    }

    /**
     * 停靠位置枚举。
     */
    public enum DockPosition {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        CENTER
    }
}
