package com.novafx.ui.components;

import javafx.scene.control.Button;

/**
 * 按钮组件 — UI Kit v1 统一组件。
 * <p>
 * 统一的按钮样式，用于所有交互按钮。
 * 使用 CSS 类 {@code .fx-button} 进行样式控制。
 * <p>
 * 样式规范：
 * <ul>
 *   <li>背景色: #A855F7 (紫色)</li>
 *   <li>文字色: 白色</li>
 *   <li>圆角: 10px</li>
 * </ul>
 */
public class FXButton extends Button {

    /**
     * 创建按钮组件实例。
     *
     * @param text 按钮文字
     */
    public FXButton(String text) {
        super(text);
        getStyleClass().add("fx-button");
    }
}
