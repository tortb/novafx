package com.novafx.ui.components;

import javafx.scene.layout.VBox;

/**
 * 可折叠面板 — UI Kit v1 核心组件。
 * <p>
 * 支持展开/折叠的面板组件，用于 Explorer、Inspector 等场景。
 * 提供统一的折叠逻辑和状态管理。
 * <p>
 * 使用示例：
 * <pre>
 * CollapsiblePanel panel = new CollapsiblePanel();
 * panel.getChildren().add(content);
 * panel.toggle(); // 切换展开/折叠状态
 * </pre>
 */
public class CollapsiblePanel extends VBox {

    private boolean expanded = true;

    /**
     * 创建可折叠面板实例。
     */
    public CollapsiblePanel() {
        getStyleClass().add("collapsible-panel");
    }

    /**
     * 切换展开/折叠状态。
     * <p>
     * 折叠时隐藏内容并取消布局管理，
     * 展开时显示内容并恢复布局管理。
     */
    public void toggle() {
        this.expanded = !expanded;
        setVisible(expanded);
        setManaged(expanded);
    }

    /**
     * 设置展开状态。
     *
     * @param expanded true 表示展开，false 表示折叠
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        setVisible(expanded);
        setManaged(expanded);
    }

    /**
     * 获取当前展开状态。
     *
     * @return true 表示展开，false 表示折叠
     */
    public boolean isExpanded() {
        return expanded;
    }
}
