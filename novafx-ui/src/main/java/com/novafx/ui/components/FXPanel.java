package com.novafx.ui.components;

import javafx.scene.layout.VBox;

/**
 * 面板基类 — UI Kit v1 统一组件。
 * <p>
 * 所有面板组件的基础类，提供统一的样式和行为。
 * 使用 CSS 类 {@code .fx-panel} 进行样式控制。
 * <p>
 * 样式规范：
 * <ul>
 *   <li>背景色: #111111</li>
 *   <li>圆角: 12px</li>
 *   <li>边框: #2A2A2A</li>
 *   <li>内边距: 12px</li>
 * </ul>
 */
public abstract class FXPanel extends VBox {

    /**
     * 创建面板基类实例。
     * 自动添加 CSS 类 {@code .fx-panel}。
     */
    public FXPanel() {
        getStyleClass().add("fx-panel");
    }
}
