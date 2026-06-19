package com.novafx.ui.components;

import javafx.scene.control.TextField;

/**
 * 输入框组件 — UI Kit v1 统一组件。
 * <p>
 * 统一的输入框样式，用于所有文本输入场景。
 * 使用 CSS 类 {@code .fx-input} 进行样式控制。
 * <p>
 * 样式规范：
 * <ul>
 *   <li>背景色: #1A1A1A</li>
 *   <li>文字色: 白色</li>
 *   <li>圆角: 12px</li>
 *   <li>边框: #2A2A2A</li>
 * </ul>
 */
public class FXInput extends TextField {

    /**
     * 创建输入框组件实例。
     * 自动添加 CSS 类 {@code .fx-input}。
     */
    public FXInput() {
        getStyleClass().add("fx-input");
    }
}
