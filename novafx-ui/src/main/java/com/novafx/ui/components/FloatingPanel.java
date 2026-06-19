package com.novafx.ui.components;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * 浮动面板 — UI Kit v2 核心组件。
 * <p>
 * 支持自由拖拽和停靠的面板组件。
 * 可以在 DockPane 中自由移动，也可以停靠到指定位置。
 */
public class FloatingPanel extends FXPanel {

    /** 是否正在拖拽 */
    private boolean dragging = false;

    /** 拖拽起始位置 */
    private double dragStartX;
    private double dragStartY;

    /** 面板初始位置 */
    private double initialX;
    private double initialY;

    /** 是否可以拖拽 */
    private boolean draggable = true;

    /** 是否已停靠 */
    private boolean docked = false;

    /** 停靠位置 */
    private DraggableDockPane.DockPosition dockPosition;

    /** 面板标题 */
    private String title;

    /**
     * 创建浮动面板实例。
     *
     * @param title 面板标题
     */
    public FloatingPanel(String title) {
        this.title = title;
        getStyleClass().add("floating-panel");
        initDragHandlers();
    }

    /**
     * 初始化拖拽事件处理器。
     */
    private void initDragHandlers() {
        addEventHandler(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        addEventHandler(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
        addEventHandler(MouseEvent.MOUSE_MOVED, this::onMouseMoved);
    }

    /**
     * 鼠标按下事件处理。
     *
     * @param event 鼠标事件
     */
    private void onMousePressed(MouseEvent event) {
        if (draggable && isHeaderArea(event)) {
            dragging = true;
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
            initialX = getLayoutX();
            initialY = getLayoutY();

            // 提升层级
            toFront();

            // 设置拖拽光标
            setCursor(Cursor.MOVE);

            event.consume();
        }
    }

    /**
     * 鼠标拖拽事件处理。
     *
     * @param event 鼠标事件
     */
    private void onMouseDragged(MouseEvent event) {
        if (dragging) {
            double deltaX = event.getSceneX() - dragStartX;
            double deltaY = event.getSceneY() - dragStartY;

            setLayoutX(initialX + deltaX);
            setLayoutY(initialY + deltaY);

            event.consume();
        }
    }

    /**
     * 鼠标释放事件处理。
     *
     * @param event 鼠标事件
     */
    private void onMouseReleased(MouseEvent event) {
        if (dragging) {
            dragging = false;
            setCursor(Cursor.DEFAULT);

            // 检查是否可以停靠
            checkDocking(event);

            event.consume();
        }
    }

    /**
     * 鼠标移动事件处理（更新光标）。
     *
     * @param event 鼠标事件
     */
    private void onMouseMoved(MouseEvent event) {
        if (draggable && isHeaderArea(event)) {
            setCursor(Cursor.MOVE);
        } else {
            setCursor(Cursor.DEFAULT);
        }
    }

    /**
     * 检查是否在标题区域（用于拖拽）。
     *
     * @param event 鼠标事件
     * @return true 表示在标题区域
     */
    private boolean isHeaderArea(MouseEvent event) {
        // 简化实现：假设顶部 30px 是标题区域
        return event.getY() < 30;
    }

    /**
     * 检查是否可以停靠到 DockPane。
     *
     * @param event 鼠标事件
     */
    private void checkDocking(MouseEvent event) {
        // 获取父容器
        if (getParent() instanceof DraggableDockPane) {
            DraggableDockPane dockPane = (DraggableDockPane) getParent();
            // 这里可以添加停靠逻辑
        }
    }

    /**
     * 停靠到指定位置。
     *
     * @param position 停靠位置
     */
    public void dock(DraggableDockPane.DockPosition position) {
        this.docked = true;
        this.dockPosition = position;
        getStyleClass().add("docked");
    }

    /**
     * 取消停靠，变为浮动状态。
     */
    public void undock() {
        this.docked = false;
        this.dockPosition = null;
        getStyleClass().remove("docked");
    }

    /**
     * 检查是否已停靠。
     *
     * @return true 表示已停靠
     */
    public boolean isDocked() {
        return docked;
    }

    /**
     * 获取停靠位置。
     *
     * @return 停靠位置，如果未停靠则返回 null
     */
    public DraggableDockPane.DockPosition getDockPosition() {
        return dockPosition;
    }

    /**
     * 设置是否可以拖拽。
     *
     * @param draggable true 表示可以拖拽
     */
    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    /**
     * 检查是否可以拖拽。
     *
     * @return true 表示可以拖拽
     */
    public boolean isDraggable() {
        return draggable;
    }

    /**
     * 获取面板标题。
     *
     * @return 面板标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置面板标题。
     *
     * @param title 面板标题
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
