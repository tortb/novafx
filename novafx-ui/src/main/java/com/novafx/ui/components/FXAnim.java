package com.novafx.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * 动画工具类 — UI Kit v1 动画系统。
 * <p>
 * 提供统一的动画效果，用于提升 UI 质感。
 * 所有动画时长控制在 150-180ms，避免影响性能。
 * <p>
 * 交互规则：
 * <ul>
 *   <li>hover: 120ms</li>
 *   <li>click: 80ms</li>
 *   <li>panel open: 180ms</li>
 *   <li>value change: 100ms</li>
 * </ul>
 */
public final class FXAnim {

    /** 默认淡入时长：150ms */
    private static final Duration FADE_DURATION = Duration.millis(150);

    /** 默认展开时长：180ms */
    private static final Duration EXPAND_DURATION = Duration.millis(180);

    // 私有构造函数，防止实例化
    private FXAnim() {
        throw new UnsupportedOperationException("FXAnim is a utility class");
    }

    /**
     * 淡入动画。
     * <p>
     * 节点从完全透明渐变到完全不透明。
     *
     * @param node 目标节点
     */
    public static void fadeIn(Node node) {
        FadeTransition ft = new FadeTransition(FADE_DURATION, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * 淡出动画。
     * <p>
     * 节点从完全不透明渐变到完全透明。
     *
     * @param node 目标节点
     */
    public static void fadeOut(Node node) {
        FadeTransition ft = new FadeTransition(FADE_DURATION, node);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.play();
    }

    /**
     * 展开动画。
     * <p>
     * 面板从缩小状态展开到正常大小。
     *
     * @param panel 目标面板
     */
    public static void expand(VBox panel) {
        ScaleTransition st = new ScaleTransition(EXPAND_DURATION, panel);
        st.setFromY(0.8);
        st.setToY(1.0);
        st.play();
    }

    /**
     * 折叠动画。
     * <p>
     * 面板从正常大小缩小到隐藏状态。
     *
     * @param panel 目标面板
     */
    public static void collapse(VBox panel) {
        ScaleTransition st = new ScaleTransition(EXPAND_DURATION, panel);
        st.setFromY(1.0);
        st.setToY(0.8);
        st.play();
    }

    /**
     * 缩放动画。
     * <p>
     * 节点从原始大小缩放到目标大小。
     *
     * @param node      目标节点
     * @param fromScale 起始缩放比例
     * @param toScale   目标缩放比例
     */
    public static void scale(Node node, double fromScale, double toScale) {
        ScaleTransition st = new ScaleTransition(EXPAND_DURATION, node);
        st.setFromX(fromScale);
        st.setFromY(fromScale);
        st.setToX(toScale);
        st.setToY(toScale);
        st.play();
    }
}
