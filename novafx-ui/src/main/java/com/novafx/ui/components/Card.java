package com.novafx.ui.components;

import javafx.scene.layout.VBox;

/**
 * Card 组件 — UI Kit v1 统一组件。
 * <p>
 * 用于 Explorer 等场景的卡片式布局。
 * 使用 CSS 类 {@code .fx-card} 进行样式控制。
 * <p>
 * 样式规范：
 * <ul>
 *   <li>背景色: #151515</li>
 *   <li>圆角: 10px</li>
 *   <li>内边距: 10px</li>
 * </ul>
 */
public class Card extends VBox {

    /**
     * 创建 Card 组件实例。
     * 自动添加 CSS 类 {@code .fx-card}。
     */
    public Card() {
        getStyleClass().add("fx-card");
    }
}
